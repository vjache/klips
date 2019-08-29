package org.klips.dsl

import org.klips.db.DatabaseImpl
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.util.Log
import org.klips.db.TestDomainTupleFactory
import org.klips.engine.rete.builder.AgendaManager
import org.klips.engine.rete.db.StrategyOneDB


class GameRulesDBTest : GameRulesTest() {
    override fun createGameRules(): GameRules {
        return object : GameRules(){
            override fun createEngine(log: Log, rules: List<RuleClause>, agendaManager: AgendaManager): ReteBuilderStrategy {
                return StrategyOneDB(DatabaseImpl(), TestDomainTupleFactory(), log, rules, agendaManager)
            }
        }
    }
}