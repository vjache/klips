package org.klips.engine.rete.db

import org.klips.engine.rete.db.Serializer
import org.klips.dsl.Facet.ConstFacet
import org.klips.dsl.facet
import org.klips.dsl.ref
import org.klips.engine.Binding
import org.klips.engine.ComposeBinding
import org.klips.engine.Modification
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.engine.SingletonBinding
import org.klips.engine.query.BindingSet
import org.klips.engine.query.SimpleMappedBindingSet
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.util.putIfAbsent
import org.klips.engine.util.to
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UNCHECKED_CAST")
class BetaNodeDB(strategy: StrategyOneDB, f1: Node, f2: Node) : BetaNode(strategy.log, f1, f2), BindingRepo {

    val rId by lazy { strategy.rIds.andIncrement }

    private val ids = AtomicInteger(0) // bindings ids

    private val bIdRef = ref<Int>("##BINDING_ID##")
    private val indexRefs = commonRefs.toList().plus(bIdRef)

    private val leftIndex: MutableMap<Binding, MutableSet<Binding>> by lazy {
        strategy.db.openMultiMap(
                "b-node_db_left_$rId",
                BindingComparator(indexRefs),
                BindingSerializerDB(indexRefs, strategy.tupleFactory),
                BindingSerializerDB(f1.refs, strategy.tupleFactory))
    }

    private val rightIndex: MutableMap<Binding, MutableSet<Binding>> by lazy {
        strategy.db.openMultiMap(
                "b-node_db_right_$rId",
                BindingComparator(indexRefs),
                BindingSerializerDB(indexRefs, strategy.tupleFactory),
                BindingSerializerDB(f2.refs, strategy.tupleFactory))
    }

    private val bindings: MutableMap<Int, Binding> by lazy {
        strategy.db.openMap(
                "b-node_db_cache_$rId",
                Comparator { t1, t2 -> t1 - t2 },
                Serializer.INT,
                BindingSerializerDB(refs, strategy.tupleFactory))
    }

    override fun fetchBinding(id: Int) = BindingDB(id, bindings[id]!!)

    val bindingsRev: MutableMap<Binding, Int> by lazy {
        strategy.db.openMap(
                "b-node_db_cache_rev_$rId",
                BindingComparator(refs),
                BindingSerializerDB(refs, strategy.tupleFactory),
                Serializer.INT)
    }

    override fun modifyIndex(source: Node, key: Binding, mdf: Modification<Binding>,
                             hookModify:() -> Unit): Boolean {
        val bId = (mdf.arg as BindingDB).dbId
        val index = when (source) {
            left -> leftIndex
            right -> rightIndex
            else -> throw IllegalArgumentException("Bad source: $source")
        }
        return when(mdf){
            is Assert -> {
                hookModify()
                index[key]!!.add(SingletonBinding(bIdRef to bId.facet))
            }
            is Retire -> {
                val modified = index[key]!!.remove(SingletonBinding(bIdRef to bId.facet))
                hookModify()
                modified
            }
        }
    }

    override fun lookupIndex(source: Node, key: Binding): BindingSet {

        return when (source) {
            left -> SimpleMappedBindingSet(left.refs, leftIndex[key]!!) {
                val bId = (it[bIdRef] as ConstFacet<Int>).value
                (left as BindingRepo).fetchBinding(bId)
            }
            right -> SimpleMappedBindingSet(right.refs, rightIndex[key]!!) {
                val bId = (it[bIdRef] as ConstFacet<Int>).value
                (right as BindingRepo).fetchBinding(bId)
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
            is Assert -> {
                bId = ids.andIncrement
                bindings.putIfAbsent(bId, binding)?.let {
                    throw IllegalStateException()
                }
                bindingsRev.putIfAbsent(binding, bId)
            }
            is Retire -> {
                bId = bindingsRev.remove(binding)!!
                bindings.remove(bId)
            }
            else -> throw IllegalArgumentException()
        }

        return BindingDB(bId, binding)
    }
}
