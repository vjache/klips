package org.klips.engine.rete.builder.optimizer

import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding
import org.klips.engine.rete.AlphaNode
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.rete.ProxyNode


abstract class Tree : Cloneable {
    abstract val reteNode: Node
    abstract val signature: Signature
    abstract val junctLeafsMap: Map<FacetRef<*>, Set<Leaf>>
    public abstract override fun clone(): Tree
    abstract fun bind(t: Tree): Binding?

    val junctLeafs: Set<Set<Leaf>> by lazy {
        junctLeafsMap.values.filter { it.size > 1 }.toSet()
    }

    fun deepTraverse(visit: Tree.() -> Boolean) : Tree? {
        if (visit())
            return this
        when(this){
            is Fork -> {
                val leftResult = left.deepTraverse(visit)
                if (leftResult != null){
                    return this
                } else
                    return right.deepTraverse(visit)
            }
            is Rename -> {
                return tree.deepTraverse(visit)
            }
            else -> return null
        }
    }

//    fun addToIndex(index:MutableMap<Signature, MutableSet<Tree>>) {
//        deepTraverse {
//            index.getOrPut(signature){
//                mutableSetOf()
//            }.add(this)
//            false
//        }
//    }
//
//    fun findMaxMatch(index:Map<Signature, MutableSet<Tree>>): Triple<Tree, Tree, Binding>? {
//        index.values.activation { it.size > 1 }.reversed().forEach { group0 ->
//            val group  = group0.toList()
//
//            for(i in 0..group.size-1)
//                for(j in i+1..group.size-1){
//                    group[i].bind(group[j])?.let {
//                        return Triple(group[i], group[j], it)
//                    }
//                }
//        }
//        return null
//    }
//
//    fun findMaxMatch(): Triple<Tree, Tree, Binding>? {
//        val index = sortedMapOf<Signature, MutableSet<Tree>>()
//
//        addToIndex(index)
//
//        return findMaxMatch(index)
//    }

    fun makeTree(n: Node):Tree = when (n) {
        is BetaNode  -> Fork(n)
        is AlphaNode -> Leafs(n)
        is ProxyNode -> Rename(n)
        else -> throw IllegalArgumentException()
    }
}

/////////////////////////////////