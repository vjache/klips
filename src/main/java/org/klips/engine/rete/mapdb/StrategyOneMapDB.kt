package org.klips.engine.rete.mapdb

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.query.BindingSet
import org.klips.engine.rete.AlphaNode
import org.klips.engine.rete.BetaNode
import org.klips.engine.rete.Node
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.StrategyOne
import org.klips.engine.util.Log
import org.mapdb.DBMaker

class StrategyOneMapDB (log: Log, patterns: List<RuleClause>) : StrategyOne(log, patterns) {

    private val db = DBMaker.memoryDB().make()

    constructor(patterns: List<RuleClause>) : this(Log(), patterns)
    constructor(vararg patts : RuleClause) : this(Log(), mutableListOf(*patts))

    override fun createBetaNode(f1: Node, f2: Node): BetaNode {
        return BetaNodeMapDB(this, f1, f2)
    }

    override fun createAlphaNode(f1: Fact): AlphaNode {
        return AlphaNodeMapDB(this,f1)
    }
}

class AlphaNodeMapDB(strategy: StrategyOneMapDB, f1: Fact) : AlphaNode(strategy.log, f1) {
    override fun modifyCache(mdf: Modification<out Binding>): Binding? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}

class BetaNodeMapDB(strategy: StrategyOneMapDB, f1: Node, f2: Node) : BetaNode(strategy.log, f1, f2) {
    override fun modifyIndex(source: Node, key: Binding, mdf: Modification<Binding>): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun lookupIndex(source: Node, key: Binding): BindingSet {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun composeBinding(source: Node, newBinding: Binding, cachedBinding: Binding): Binding {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
