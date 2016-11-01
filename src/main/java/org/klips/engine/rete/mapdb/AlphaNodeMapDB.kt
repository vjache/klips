package org.klips.engine.rete.mapdb

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.AlphaNode
import org.mapdb.BTreeMap
import org.mapdb.Serializer
import java.util.concurrent.atomic.AtomicInteger

class AlphaNodeMapDB(val strategy: StrategyOneMapDB, f1: Fact) : AlphaNode(strategy.log, f1), BindingDB {
    override fun fetchBinding(id: Int) = BindingMapDB(id,alphaBindingsRev[id]!!)

    val rId:Int by lazy { strategy.rIds.andIncrement }

    private val alphaBindings:BTreeMap<Binding, Int> by lazy {
        strategy.db.treeMap(
                "a-node_db_$rId",
                BindingSerializer(f1.refs, strategy.tupleFactory),
                Serializer.INTEGER).createOrOpen()
    }
    private val alphaBindingsRev:BTreeMap<Int, Binding> by lazy {
        strategy.db.treeMap(
                "a-node_db_rev_$rId",
                Serializer.INTEGER,
                BindingSerializer(f1.refs, strategy.tupleFactory)).createOrOpen()
    }
    private val ids = AtomicInteger(0) // bindings ids

    override fun modifyCache(mdf: Modification<out Binding>): Binding? {
        when (mdf) {
            is Modification.Assert -> {
                val id = ids.andIncrement
                if(alphaBindings.putIfAbsent(mdf.arg, id) == null) {
                    val prev = alphaBindingsRev.putIfAbsent(id, mdf.arg)
                    if(prev != null)
                        throw IllegalStateException("Expected null value for id = $id but found $prev .")
                    return BindingMapDB(id, mdf.arg)
                }
            }

            is Modification.Retire -> {
                val id = alphaBindings.remove(mdf.arg)
                if (id != null) {
                    alphaBindingsRev.remove(id)
                    return BindingMapDB(id, mdf.arg)
                }
            }
        }
        return null
    }
}