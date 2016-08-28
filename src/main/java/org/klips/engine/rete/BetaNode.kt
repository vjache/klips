package org.klips.engine.rete

import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.ProjectBinding
import org.klips.engine.query.BindingSet

abstract class BetaNode(left: Node, right: Node) : Node(), Consumer {

    override val refs: Set<FacetRef<*>> = left.refs.union(right.refs)

    val commonRefs: Set<FacetRef<*>> = left.refs.intersect(right.refs)

        var left: Node = left
            set(value) {
                value.addConsumer(this)
                field = value
            }

    var right: Node = right
        set(value) {
            value.addConsumer(this)
            field = value
        }

    init {
        left.addConsumer(this)
        right.addConsumer(this)
    }

    protected fun otherSource(source: Node) = when (source) {
        left -> right
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
            newBinding: Binding,
            cachedBinding: Binding): Binding

    override fun consume(source: Node, mdf: Modification<Binding>) {
        val binding = mdf.arg
        val key = makeKey(binding)
        if(modifyIndex(source, key, mdf)) {
            val lookupResults = lookupIndex(otherSource(source), key)
            val lookupResultsCopy = lookupResults.map { it }
            lookupResultsCopy.forEach {
                notifyConsumers(mdf.inherit(composeBinding(source, binding, it)))
            }
        }
    }


    fun replace(with: Node, which: Node, binding: Binding) {
        // 1. create proxy node ProxyNode
        val pnode = ProxyNode(with, binding)
        // 2. attach proxy node to 'with'
        val consumersRemove = mutableListOf<Consumer>()
        which.consumers.forEach { consumer ->
            when (consumer) {
                is BetaNode -> {
                    // ASSERT
                    if (consumer.left != which && consumer.right != which)
                        throw IllegalArgumentException()

                    if (consumer.left == which) {
                        consumer.left = pnode
                        consumersRemove.add(consumer)
                    }

                    if (consumer.right == which) {
                        consumer.right = pnode
                        consumersRemove.add(consumer)
                    }
                }
                is ProxyNode ->
                {
                    if (consumer.node == which) {
                        consumer.node = pnode
                        consumersRemove.add(consumer)
                    }
                }
                else ->
                    throw IllegalArgumentException()
            }
        }

        // Clean consumers
        consumersRemove.forEach { which.removeConsumer(it) }
    }

    private fun makeKey(binding: Binding) = ProjectBinding(commonRefs, binding)

    override fun toString() = "B-Node($refs) [${System.identityHashCode(this)}]"
}

