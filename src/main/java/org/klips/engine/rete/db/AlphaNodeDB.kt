package org.klips.engine.rete.db

import org.klips.engine.rete.db.Serializer
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.AlphaNode
import org.klips.engine.util.putIfAbsent
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class AlphaNodeDB(val strategy: StrategyOneDB, f1: Fact) : AlphaNode(strategy.log, f1), BindingRepo {
    override fun fetchBinding(id: Int): BindingDB {
        val b = alphaBindingsRev[id]
        return BindingDB(id, b!!)
    }

    val rId:Int by lazy { strategy.rIds.andIncrement }

    private val alphaBindings:MutableMap<Binding, Int> by lazy {
        strategy.db.openMap(
                "a-node_db_$rId",
                BindingComparator(f1.refs),
                BindingSerializerDB(f1.refs, strategy.tupleFactory),
                Serializer.INT)
    }
    private val alphaBindingsRev:MutableMap<Int, Binding> by lazy {
        strategy.db.openMap(
                "a-node_db_rev_$rId",
                Comparator { t1, t2 -> t1 - t2 },
                Serializer.INT,
                BindingSerializerDB(f1.refs, strategy.tupleFactory))
    }
    private val ids = AtomicInteger(0) // bindings ids

    override fun modifyCache(mdf: Modification<out Binding>, hookModify: (Binding) -> Unit): Boolean {
        when (mdf) {
            is Modification.Assert -> {
                if (mdf.arg in alphaBindings.keys) return false
                val id = ids.andIncrement
                val dbBinding = BindingDB(id, mdf.arg)

                alphaBindings.putIfAbsent(mdf.arg, id)
                alphaBindingsRev.putIfAbsent(id, mdf.arg)?.let { throw IllegalStateException("Expected null value for id = $id but found $it.") }
                hookModify(dbBinding)
                return true
            }

            is Modification.Retire -> {
                val id = alphaBindings[mdf.arg] ?: return false
                hookModify(BindingDB(id, mdf.arg))
                alphaBindings.remove(mdf.arg)
                alphaBindingsRev.remove(id)
                return true
            }
        }
    }
}