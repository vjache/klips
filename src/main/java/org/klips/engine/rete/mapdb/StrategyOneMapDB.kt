package org.klips.engine.rete.mapdb

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.rete.AlphaNode
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.rete.ProxyNode
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.StrategyOne
import org.klips.engine.util.Log
import org.mapdb.BTreeMap
import org.mapdb.DBMaker
import java.util.concurrent.atomic.AtomicInteger

class StrategyOneMapDB (val tupleFactory: TupleFactory?, log: Log, patterns: List<RuleClause>) : StrategyOne(log, patterns) {

    internal val db      = DBMaker.memoryDB().make()
    internal val rIds    = AtomicInteger(0)

    constructor(patterns: List<RuleClause>) : this(null, Log(), patterns)
    constructor(vararg patts : RuleClause) : this(null, Log(), mutableListOf(*patts))

    override fun createBetaNode(f1: Node, f2: Node): BetaNode {
        return BetaNodeMapDB(this, f1, f2)
    }

    override fun createAlphaNode(f1: Fact): AlphaNode {
        return AlphaNodeMapDB(this, f1)
    }

    override fun createProxyNode(node: Node, renamingData: Binding):ProxyNode = object : ProxyNode(log, node, renamingData),BindingDB{
        override fun fetchBinding(id: Int) = proxifyBinding((node as BindingDB).fetchBinding(id))

        override fun proxifyBinding(former: Binding): Binding {
            val id = (former as BindingMapDB).dbId
            return BindingMapDB(id, ProxyBinding(former))
        }
    }
}