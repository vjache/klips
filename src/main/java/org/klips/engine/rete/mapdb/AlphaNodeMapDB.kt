package org.klips.engine.rete.mapdb

import com.sun.org.apache.xpath.internal.operations.Bool
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.AlphaNode
import org.mapdb.BTreeMap
import org.mapdb.Serializer
import java.util.concurrent.atomic.AtomicInteger

class AlphaNodeMapDB(val strategy: StrategyOneMapDB, f1: Fact) : AlphaNode(strategy.log, f1), BindingDB {
    override fun fetchBinding(id: Int): BindingMapDB {
        val b = alphaBindingsRev[id]
        return BindingMapDB(id, b!!)
    }

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

    override fun modifyCache(mdf: Modification<out Binding>, hookModify: (Binding) -> Unit): Boolean {
        when (mdf) {
            is Modification.Assert -> {
                if (mdf.arg in alphaBindings.keys) return false
                val id = ids.andIncrement
                val dbBinding = BindingMapDB(id, mdf.arg)
                alphaBindings.putIfAbsent(mdf.arg, id)
                alphaBindingsRev.putIfAbsent(id, mdf.arg)?.let { throw IllegalStateException("Expected null value for id = $id but found $it.") }
                hookModify(dbBinding)
                return true
            }

            is Modification.Retire -> {
                val id = alphaBindings[mdf.arg] ?: return false
                hookModify(BindingMapDB(id, mdf.arg))
                alphaBindings.remove(mdf.arg)
                alphaBindingsRev.remove(id)
                return true
            }
        }
    }
}