package org.klips.engine.rete.builder.optimizer

import org.klips.engine.Binding
import org.klips.engine.rete.*
import org.klips.engine.util.Log


class Optimizer(val log:Log) {

    fun optimize(nodes: Set<Node>): Set<Node> {

        val roots = detectRoots(nodes)

        val replaced = mutableSetOf<Node>()

        fun step():Boolean {
            val index = sortedMapOf<Signature, MutableSet<Tree>>()
            roots.forEach {
                nodeToTree(it).addToIndex(index)
            }

            findMaxMatch(index)?.let {
                val (t1, t2, b) = it
                replace(t1.reteNode, t2.reteNode, b)
                replaced.add(t2.reteNode)
                return true
            }
            return false
        }

        while (step()){}

        return replaced
    }

//    fun optimize1(nodes: Set<Node>): Set<Node> {
//
//        val roots = detectRoots(nodes)
//
//        val replaced = mutableSetOf<Node>()
//
//        roots.forEach { root ->
//
//            fun step():Boolean {
//                nodeToTree(root).findMaxMatch()?.let {
//                    val (t1, t2, b) = it
//                    replace(t1.reteNode, t2.reteNode, b)
//                    replaced.add(t2.reteNode)
//                    return true
//                }
//                return false
//            }
//
//            while (step()){}
//        }
//
//        return replaced
//    }

    private fun nodeToTree(it: Node): Tree {
        return when (it) {
            is BetaNode -> Fork(it)
            is AlphaNode -> Leafs(it)
            is ProxyNode -> Rename(it)
            else -> throw IllegalArgumentException()
        }
    }

    private fun detectRoots(nodes: Set<Node>): List<Node> {
        // Detect non root nodes
        val nonRoots = mutableSetOf<Node>().apply {
            nodes.forEach { node ->
                if (node is BetaNode) {
                    add(node.left)
                    add(node.right)
                } else add(node)
            }
        }

        // Detect root nodes
        val roots = nodes.filter { it !in nonRoots }
        return roots
    }

    fun replace(with: Node, which: Node, binding: Binding) {
        // 1. create proxy node ProxyNode
        val pnode = ProxyNode(log, with, binding)
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

    fun Tree.addToIndex(index:MutableMap<Signature, MutableSet<Tree>>) {
        deepTraverse {
            index.getOrPut(signature){
                mutableSetOf()
            }.add(this)
            false
        }
    }

    fun findMaxMatch(index:Map<Signature, MutableSet<Tree>>): Triple<Tree, Tree, Binding>? {
        index.values.filter { it.size > 1 }.reversed().forEach { group0 ->
            val group  = group0.toList()

            for(i in 0..group.size-1)
                for(j in i+1..group.size-1){
                    if (group[i].reteNode !== group[j].reteNode) {
                        group[i].bind(group[j])?.let {
                            return Triple(group[i], group[j], it)
                        }
                    }
                }
        }
        return null
    }
}