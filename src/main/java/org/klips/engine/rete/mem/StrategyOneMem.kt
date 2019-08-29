package org.klips.engine.rete.mem

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.rete.Node
import org.klips.engine.rete.ProxyNode
import org.klips.engine.rete.builder.AgendaManager
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.PriorityAgendaManager
import org.klips.engine.rete.builder.StrategyOne
import org.klips.engine.util.Log

class StrategyOneMem(log: Log, patterns: List<RuleClause>, agendaManager: AgendaManager) : StrategyOne(agendaManager, log, patterns) {

    constructor(patterns: List<RuleClause>) : this(Log(), patterns, PriorityAgendaManager())
    constructor(vararg patts : RuleClause) : this(Log(), mutableListOf(*patts), PriorityAgendaManager())

    override fun createBetaNode(f1: Node, f2: Node) = MemBetaNode(log,f1,f2)
    override fun createAlphaNode(f1: Fact) = MemAlphaNode(log,f1)
    override fun createProxyNode(node: Node, renamingData: Binding) = object : ProxyNode(log, node, renamingData){
        override fun proxifyBinding(former: Binding) = ProxyBinding(former)
    }
}