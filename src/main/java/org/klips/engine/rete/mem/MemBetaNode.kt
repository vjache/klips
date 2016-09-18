package org.klips.engine.rete.mem

import org.klips.engine.Binding
import org.klips.engine.ComposeBinding
import org.klips.engine.Modification
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.engine.query.BindingSet
import org.klips.engine.query.SimpleBindingSet
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.util.Log

class MemBetaNode(log: Log, l: Node, r: Node) : BetaNode(log, l, r) {

    private val leftIndex = mutableMapOf<Binding, MutableSet<Binding>>()
    private val rightIndex = mutableMapOf<Binding, MutableSet<Binding>>()

    override fun modifyIndex(source: Node, key: Binding, mdf: Modification<Binding>): Boolean {
        val values = when (source) {
            left  -> leftIndex
            right -> rightIndex
            else -> throw IllegalArgumentException()
        }.getOrPut(key) { mutableSetOf() }
        val value = mdf.arg
        when(mdf){
            is Assert -> {
                if (value in values)
                    return false
                else {
                    values.add(value)
                    return true
                }
            }
            is Retire -> {
                if (value !in values)
                    return false
                else {
                    values.remove(value)
                    return true
                }
            }
        }
    }

    override fun lookupIndex(source: Node, key: Binding): BindingSet {
        return SimpleBindingSet(refs, when (source) {
            left  -> leftIndex[key]
            right -> rightIndex[key]
            else  -> throw IllegalArgumentException()
        } ?: emptySet())
    }

    override fun composeBinding(
            source: Node,
            newBinding: Binding,
            cachedBinding: Binding) = when (source) {
        left  -> ComposeBinding(newBinding, cachedBinding)
        right -> ComposeBinding(cachedBinding, newBinding)
        else  -> throw IllegalArgumentException()
    }
//
//    override fun equals(other: Both?): Boolean {
//        if (other !is BetaNode) return false
//
//        return left.equals(other.left) && right.equals(other.right)
//    }
//
//    override fun hashCode(): Int {
//        return left.hashCode() + (right.hashCode() shl 1)
//    }
//
//    override fun toString() = "B-Node($left, $right)"
}

