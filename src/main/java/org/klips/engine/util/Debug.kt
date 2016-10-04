package org.klips.engine.util

import org.klips.dsl.Fact
import org.klips.dsl.substitute
import org.klips.engine.rete.AlphaNode
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.rete.ProxyNode
import java.util.concurrent.atomic.AtomicInteger


fun Node.printTree(tab:String = "") {
    val happen = log.nodeActivity[this]?.first  ?: 0
    val failed = log.nodeActivity[this]?.second ?: 0
    val counters = "($happen|$failed)"
    when (this) {
        is BetaNode -> {
            println("${tab}B[${hashCode()}]$counters:   ${collectPattern(this)}")
            val tab1 = tab + "    "
            left.printTree(tab1)
            right.printTree(tab1)
        }
        is AlphaNode -> {
            println("${tab}A[${hashCode()}]$counters:   ${collectPattern(this).first()}")
        }
        is ProxyNode -> {
            println("${tab}P[${hashCode()}]$counters:   ${collectPattern(this)}")
            val tab1 = tab + "    "
            node.printTree(tab1)
        }
    }

}

fun collectPattern(n: Node):List<Fact> {
    return when (n) {
        is AlphaNode -> listOf(n.pattern)
        is BetaNode -> {
            collectPattern(n.left).plus(collectPattern(n.right))
        }
        is ProxyNode -> {
            collectPattern(n.node).substitute(n.renamingBinding)}
        else -> throw IllegalArgumentException("Unexpected node type: ${n.javaClass}")
    }
}

fun Node.activationHappen() {
    log.nodeActivity.getOrPut(this){
        Pair(AtomicInteger(0), AtomicInteger(0))
    }.first.incrementAndGet()
}

fun Node.activationFailed() {
    log.nodeActivity.getOrPut(this){
        Pair(AtomicInteger(0), AtomicInteger(0))
    }.second.incrementAndGet()
}