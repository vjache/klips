package org.klips.engine.rete.builder

import org.klips.dsl.Fact
import org.klips.engine.rete.Node
import org.klips.engine.rete.mem.MemAlphaNode
import org.klips.engine.rete.mem.MemBetaNode

class StrategyOneMem(patterns: List<RuleClause>) : StrategyOne(patterns) {

    constructor(vararg patts : RuleClause) : this(mutableListOf(*patts))

    override fun createBetaNode(f1: Node, f2: Node) = MemBetaNode(f1,f2)
    override fun createAlphaNode(f1: Fact) = MemAlphaNode(f1)
}