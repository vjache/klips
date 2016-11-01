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

    val rId by lazy { strategy.rIds.andIncrement }

    private val ids = AtomicInteger(0) // bindings ids

    private val bIdRef = ref<Int>("##BINDING_ID##")
    private val indexRefs = commonRefs.toList().plus(bIdRef)

    private val leftIndex: NavigableSet<Binding> by lazy {
        strategy.db.treeSet(
                "b-node_db_left_$rId",
                BindingSerializer(indexRefs, strategy.tupleFactory)).createOrOpen()
    }

    private val rightIndex: NavigableSet<Binding> by lazy {
        strategy.db.treeSet(
                "b-node_db_right_$rId",
                BindingSerializer(indexRefs, strategy.tupleFactory)).createOrOpen()
    }

    private val bindings: BTreeMap<Int, Binding> by lazy {
        strategy.db.treeMap(
                "b-node_db_cache_$rId",
                Serializer.INTEGER,
                BindingSerializer(refs, strategy.tupleFactory)).createOrOpen()
    }

    override fun fetchBinding(id: Int) = bindings[id]!!

    val bindingsRev: BTreeMap<Binding, Int> by lazy {
        strategy.db.treeMap(
                "b-node_db_cache_rev_$rId",
                BindingSerializer(refs, strategy.tupleFactory),
                Serializer.INTEGER).createOrOpen()
    }

    override fun modifyIndex(source: Node, key: Binding, mdf: Modification<Binding>): Boolean {
        val bId = (mdf.arg as BindingMapDB).dbId
        return when (source) {
            left -> leftIndex
            right -> rightIndex
            else -> throw IllegalArgumentException("Bad source: $source")
        }.add(ComposeBinding(key, SingletonBinding(bIdRef to bId.facet)))
    }

    override fun lookupIndex(source: Node, key: Binding): BindingSet {
        fun NavigableSet<Binding>.subSet() = subSet(
                ComposeBinding(key, SingletonBinding(bIdRef to Int.MIN_VALUE.facet)),
                ComposeBinding(key, SingletonBinding(bIdRef to Int.MAX_VALUE.facet)))

        return when (source) {
            left -> SimpleMappedBindingSet(left.refs, leftIndex.subSet()) {
                val bId = (it[bIdRef] as ConstFacet<Int>).value
                (left as BindingDB).fetchBinding(bId)
            }
            right -> SimpleMappedBindingSet(right.refs, rightIndex.subSet()) {
                val bId = (it[bIdRef] as ConstFacet<Int>).value
                (right as BindingDB).fetchBinding(bId)
            }
            else -> throw IllegalArgumentException("Bad source: $source")
        }
    }

    override fun composeBinding(source: Node, mdf: Modification<Binding>, cachedBinding: Binding): Binding {
        val binding = when (source) {
            left -> ComposeBinding(mdf.arg, cachedBinding)
            right -> ComposeBinding(cachedBinding, mdf.arg)
            else -> throw IllegalArgumentException()
        }

        val bId:Int
        when (mdf) {
            is Modification.Assert -> {
                bId = ids.andIncrement
                bindings.putIfAbsent(bId, binding)?.let {
                    throw IllegalStateException()
                }
                bindingsRev.putIfAbsent(binding, bId)
            }
            is Modification.Retire -> {
                bId = bindingsRev.remove(binding)!!
                bindings.remove(bId, binding)
            }
            else -> throw IllegalArgumentException()
        }

        return BindingMapDB(bId, binding)
    }
}
