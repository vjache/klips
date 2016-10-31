package org.klips.engine.rete.mapdb

import org.klips.dsl.Facet.ConstFacet
import org.klips.dsl.facet
import org.klips.dsl.ref
import org.klips.engine.Binding
import org.klips.engine.ComposeBinding
import org.klips.engine.Modification
import org.klips.engine.SingletonBinding
import org.klips.engine.query.BindingSet
import org.klips.engine.query.SimpleMappedBindingSet
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.util.to
import org.mapdb.BTreeMap
import org.mapdb.Serializer
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UNCHECKED_CAST")
class BetaNodeMapDB(strategy: StrategyOneMapDB, f1: Node, f2: Node) : BetaNode(strategy.log, f1, f2), BindingDB {

    val rId = strategy.rIds.andIncrement

    private val ids = AtomicInteger(0) // bindings ids

    private val bIdRef = ref<Int>("##BINDING_ID##")
    private val indexRefs = commonRefs.toList().plus(bIdRef)

    private val leftIndex: NavigableSet<Binding> = strategy.db.treeSet(
            "b-node_db_left_$rId",
            BindingSerializer(indexRefs, strategy.tupleFactory)).createOrOpen()

    private val rightIndex: NavigableSet<Binding> = strategy.db.treeSet(
            "b-node_db_right_$rId",
            BindingSerializer(indexRefs, strategy.tupleFactory)).createOrOpen()

    override val bindings: BTreeMap<Int, Binding> = strategy.db.treeMap(
            "b-node_db_cache_$rId",
            Serializer.INTEGER,
            BindingSerializer(refs, strategy.tupleFactory)).createOrOpen()

    override fun modifyIndex(source: Node, key: Binding, mdf: Modification<Binding>): Boolean {
        val bId = (mdf.arg as BindingMapDB).dbId
        return when(source) {
            left  -> leftIndex
            right -> rightIndex
            else -> throw IllegalArgumentException("Bad source: $source")
        }.add(ComposeBinding(key, SingletonBinding(bIdRef to bId.facet)))
    }

    override fun lookupIndex(source: Node, key: Binding): BindingSet {
        fun NavigableSet<Binding>.subSet() = subSet(
                ComposeBinding(key, SingletonBinding(bIdRef to Int.MIN_VALUE.facet)),
                ComposeBinding(key, SingletonBinding(bIdRef to Int.MAX_VALUE.facet)))

        return when(source) {
            left  -> SimpleMappedBindingSet(left.refs, leftIndex.subSet()) {
                val bId = (it[bIdRef] as ConstFacet<Int>).value
                (left as BindingDB).bindings[bId]!!
            }
            right -> SimpleMappedBindingSet(right.refs, rightIndex.subSet()) {
                val bId = (it[bIdRef] as ConstFacet<Int>).value
                (right as BindingDB).bindings[bId]!!
            }
            else -> throw IllegalArgumentException("Bad source: $source")
        }
    }

    override fun composeBinding(source: Node, newBinding: Binding, cachedBinding: Binding): Binding {
        val binding = when (source) {
            left -> ComposeBinding(newBinding, cachedBinding)
            right -> ComposeBinding(cachedBinding, newBinding)
            else -> throw IllegalArgumentException()
        }

        val bId = ids.andIncrement
        bindings.putIfAbsent(bId, binding)?.let {
            throw IllegalStateException()
        }

        return BindingMapDB(bId, binding)
    }

}