package org.klips.engine.rete

import org.klips.dsl.Facet
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.util.Log

abstract class Node(val log: Log) {
    abstract val refs : Set<Facet.FacetRef<*>>
    private val consumers_        = mutableListOf<Consumer>()
    val consumers :List<Consumer> = consumers_

    fun addConsumer(cons: Consumer)    = consumers_.add(cons)
    fun removeConsumer(cons: Consumer) = consumers_.remove(cons)

    protected fun notifyConsumers(mdf:Modification<Binding>) = consumers.forEach {
        it.consume(this, mdf) }

    fun deepTraverse(visit: Node.() -> Boolean) : Node? {
        if (visit())
            return this
        when(this){
            is BetaNode -> {
                val leftResult = left.deepTraverse(visit)
                if (leftResult != null){
                    return this
                } else
                    return right.deepTraverse(visit)
            }
            is ProxyNode -> {
                return node.deepTraverse(visit)
            }
            else -> return null
        }
    }

}