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
import java.util.*

class MemBetaNode(log: Log, l: Node, r: Node) : BetaNode(log, l, r) {

    private val leftIndex  = HashMap<Binding, HashSet<Binding>>()
    private val rightIndex = HashMap<Binding, HashSet<Binding>>()

    override fun modifyIndex(source: Node, key: Binding, mdf: Modification<Binding>,
                             hookModify:() -> Unit): Boolean {
        val values = when (source) {
            left  -> leftIndex
            right -> rightIndex
            else -> throw IllegalArgumentException()
        }.getOrPut(key) { HashSet(8) }
        val value = mdf.arg
        when(mdf){
            is Assert -> {
                if (value in values)
                    return false
                else {
                    values.add(value)
                    hookModify()
                    return true
                }
            }
            is Retire -> {
                if (value !in values)
                    return false
                else {
                    hookModify()
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
            mdf: Modification<Binding>,
            cachedBinding: Binding) = when (source) {
        left  -> ComposeBinding(mdf.arg, cachedBinding)
        right -> ComposeBinding(cachedBinding, mdf.arg)
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

