package org.klips.dsl

import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.mapdb.StrategyOneMapDB
import org.klips.engine.util.Log
import org.klips.mapdb.TestDomainTupleFactory


class GameRulesMapDBTest : GameRulesTest() {
    override fun createGameRules(): GameRules {
        return object : GameRules(){
            override fun createEngine(log: Log, rules: List<RuleClause>): ReteBuilderStrategy {
                return StrategyOneMapDB(TestDomainTupleFactory(), log, rules)
            }
        }
    }
}