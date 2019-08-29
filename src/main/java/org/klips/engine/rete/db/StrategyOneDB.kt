package org.klips.engine.rete.db

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.rete.AlphaNode
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.rete.ProxyNode
import org.klips.engine.rete.builder.AgendaManager
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.StrategyOne
import org.klips.engine.util.Log
import java.util.concurrent.atomic.AtomicInteger

class StrategyOneDB(val db: Database, val tupleFactory: TupleFactoryDB?, log: Log, patterns: List<RuleClause>, agendaManager: AgendaManager) : StrategyOne(agendaManager, log, patterns) {

    internal val rIds    = AtomicInteger(0)

    override fun createBetaNode(f1: Node, f2: Node): BetaNode {
        return BetaNodeDB(this, f1, f2)
    }

    override fun createAlphaNode(f1: Fact): AlphaNode {
        return AlphaNodeDB(this, f1)
    }

    override fun createProxyNode(node: Node, renamingData: Binding):ProxyNode = object : ProxyNode(log, node, renamingData), BindingRepo {
        override fun fetchBinding(id: Int) = proxifyBinding((node as BindingRepo).fetchBinding(id))

        override fun proxifyBinding(former: Binding): Binding {
            val id = (former as BindingDB).dbId
            return BindingDB(id, ProxyBinding(former))
        }
    }
}