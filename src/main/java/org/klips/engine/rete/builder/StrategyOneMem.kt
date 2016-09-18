package org.klips.engine.rete.builder

import org.klips.dsl.Fact
import org.klips.engine.rete.Node
import org.klips.engine.rete.mem.MemAlphaNode
import org.klips.engine.rete.mem.MemBetaNode
import org.klips.engine.util.Log

class StrategyOneMem(log: Log, patterns: List<RuleClause>) : StrategyOne(log, patterns) {

    constructor(patterns: List<RuleClause>) : this(Log(), patterns)
    constructor(vararg patts : RuleClause) : this(Log(), mutableListOf(*patts))

    override fun createBetaNode(f1: Node, f2: Node) = MemBetaNode(log,f1,f2)
    override fun createAlphaNode(f1: Fact) = MemAlphaNode(log,f1)
}