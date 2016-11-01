package org.klips.mapdb

import org.junit.Test
import org.klips.dsl.*
import org.klips.engine.*
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.mapdb.StrategyOneMapDB
import org.klips.engine.util.Log
import kotlin.test.assertNotNull


class SimpleRuleSetTest : RuleSet(Log()) {

    val cid = ref<CellId>("cid")
    val cid1 = ref<CellId>("cid1")
    val cid2 = ref<CellId>("cid2")
    val aid = ref<ActorId>("aid")
    val aid1 = ref<ActorId>("aid1")
    val kind = ref<ActorKind>("kind")
    val kind1 = ref<ActorKind>("kind1")
    val pid = ref<PlayerId>("pid")
    val pid1 = ref<PlayerId>("pid1")
    val nrgy = ref<Level>("nrgy")
    val nrgy1 = ref<Level>("nrgy1")
    val hlth = ref<Level>("hlth")
    val hlth1 = ref<Level>("hlth1")
    val state = ref<State>("state")
    val state1 = ref<State>("state1")

    init {

        rule(name = "Adj-Symmetry") { // Symmetry of adjacency
            +Adjacent(cid, cid1)
            effect {
                +Adjacent(cid1, cid)
                println("$it")
            }
        }

        rule(name = "CreateAgent") { // On Move
            -CreateAgentCommand(aid, cid1, kind)
            +Actor(aid = aid, type = ActorKind.Aim.facet, state = State.Deployed.facet, pid = pid)
            +At(aid, cid)
            +Adjacent(cid, cid1)

            effect { sol ->
                val newAid = ActorId()
                +Actor(newAid, sol[pid], sol[kind])
                +At(newAid.facet, cid1)
                println("CreateAgent >>>> $sol")
            }
        }
    }

    override fun createEngine(log: Log, rules: List<RuleClause>): ReteBuilderStrategy {
        return StrategyOneMapDB(TestDomainTupleFactory(), log, rules)
    }

    @Test
    fun basicTest() {
        SimpleRuleSetTest().input.flush("Adj-Symmetry") {
            for (i in 1..10)
                for (j in 1..10)
                    if(i>j) +Adjacent(i, j)
        }
    }

    @Test
    fun createCommandTest() {
        SimpleRuleSetTest().input.flush("CreateAgent") {
            +Adjacent(2, 1)
            +At(100, 1)
            +Actor(100, 1000, ActorKind.Aim, 100f, 100f, State.Deployed)
            +CreateAgentCommand(ActorId(100), CellId(2), ActorKind.Comm)
        }
        assertNotNull(ActorId.last)
    }

}