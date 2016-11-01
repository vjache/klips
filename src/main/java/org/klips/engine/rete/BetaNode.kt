package org.klips.engine.rete

import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.substitute
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.ProjectBinding
import org.klips.engine.query.BindingSet
import org.klips.engine.util.Log
import org.klips.engine.util.activationFailed
import org.klips.engine.util.activationHappen
import org.klips.engine.util.collectPattern
import java.util.concurrent.atomic.AtomicInteger

abstract class BetaNode : Node, Consumer {

    final override val refs: Set<FacetRef<*>>

    val commonRefs: Set<FacetRef<*>>

    var left: Node
        set(value) {
            value.addConsumer(this)
            field = value
        }

    var right: Node
        set(value) {
            value.addConsumer(this)
            field = value
        }

    constructor(log: Log, l: Node, r: Node):super(log) {
        left  = l
        right = r
        refs  =  l.refs.union(right.refs)
        commonRefs = left.refs.intersect(right.refs)
    }

    protected fun otherSource(source: Node) = when (source) {
        left  -> right
        right -> left
        else -> throw IllegalArgumentException("No such source $source.")
    }

    protected abstract fun modifyIndex(
            source: Node,
            key: Binding,
            mdf: Modification<Binding>): Boolean

    protected abstract fun lookupIndex(
            source: Node,
            key: Binding): BindingSet

    protected abstract fun composeBinding(
            source: Node,
            mdf: Modification<Binding>,
            cachedBinding: Binding): Binding

    fun other(n: Node) = when (n) {
        left -> right
        right -> left
        else -> throw IllegalArgumentException("Failed to get complement node: $n")
    }

    companion object dbg {
        val cnt = AtomicInteger(0)
    }

    override fun consume(source: Node, mdf: Modification<Binding>) {
        val binding = mdf.arg
        val key = makeKey(binding)
        if (modifyIndex(source, key, mdf)) {
            val lookupResults = lookupIndex(otherSource(source), key)
            dbg.cnt.andIncrement
            val patt1 = collectPattern(source)
            val patt2 = collectPattern(other(source))

            log.reteEvent {
                if (lookupResults.size == 0) {
                    activationFailed()
                    val hcs = "[${hashCode()}:${left.hashCode()},${right.hashCode()}()]"
                    "JOIN FAIL(${dbg.cnt})$hcs: \n\t$key\n\t${patt1.substitute(mdf.arg)}\n\t$patt2"
                } else null
            }

            val lookupResultsCopy = lookupResults.map { it }
            lookupResultsCopy.forEach {
                log.reteEvent {
                    activationHappen()
                    val hcs = "[${hashCode()}:${left.hashCode()},${right.hashCode()}()]"
                    "JOIN HAPPEN(${dbg.cnt})$hcs: \n\t$key\n\t${patt1.substitute(mdf.arg)}\n\t${patt2.substitute(it)}"
                }
                notifyConsumers(mdf.inherit(composeBinding(source, mdf, it)))
            }
        }
    }

    private fun makeKey(binding: Binding) = ProjectBinding(commonRefs, binding)

    override fun toString() = "B-Node($refs) [${System.identityHashCode(this)}]"
}

