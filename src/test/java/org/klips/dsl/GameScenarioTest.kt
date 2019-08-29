package org.klips.dsl

import org.junit.Test
import org.klips.RuleGroupNotTriggeredException
import org.klips.engine.*
import org.klips.engine.rete.ReteInput
import kotlin.test.assertFailsWith

class GameScenarioTest {

    val pid1 = PlayerId(1)
    val pid2 = PlayerId(2)

    @Test
    fun main() {
        val input = GameRules().input

        with(input) {

            var aims: Pair<ActorId, ActorId>? = null
            flush("Adj-Symmetry") {
                createSpace()
                aims = placeAims()
            }

            val (aim1, aim2) = aims!!

            println(aims)

            // Create Communication agent by AIM 1
            flush("CreateAgent")
            { +CreateAgentCommand(aim1, cid(10, 11).value, ActorKind.Comm) }
            val comm1 = ActorId.last!!
            println("COMM: $comm1")

            // Create Communication agent by AIM 2
            flush("CreateAgent")
            { +CreateAgentCommand(aim2, cid(90, 91).value, ActorKind.Comm) }

            // Move Communication agent to cell (10,13)
            for (i in 12..13)
            flush("Move")
            { +MoveCommand(comm1.facet, cid(10, i)) }

            // Deploy Communication agent
            flush("Deploy")
            { +DeployCommand(comm1.facet) }

            // Create Guard agent by AIM 1
            flush("CreateAgent")
            { +CreateAgentCommand(aim1, cid(10, 11).value, ActorKind.Guard) }
            val guard1 = ActorId.last!!

            // Move Guard agent to cell (10,12)
            flush("Move")
            { +MoveCommand(guard1.facet, cid(10, 12)) }

            // Move Guard agent to cell (11,12)
            flush("Move")
            { +MoveCommand(guard1.facet, cid(11, 12)) }

            // Try move Guard agent to cell (12,12) => must fail due to out of comm field
            assertFailsWith<RuleGroupNotTriggeredException> {
                flush("Move")
                { +MoveCommand(guard1.facet, cid(12, 12)) }
            }

            // Try move Guard agent to cell (12,12) => must fail due to out of comm field
            assertFailsWith<RuleGroupNotTriggeredException> {
                flush("Move")
                { +MoveCommand(guard1.facet, cid(11, 11)) }
            }

            // Move Guard agent to (11,13)
            flush("Move")
            { +MoveCommand(guard1.facet, cid(11, 13)) }

            // Move Guard agent to (12,13)
            flush("Move")
            { +MoveCommand(guard1.facet, cid(12, 13)) }

            // Try move Guard agent to cell (13,13) => must fail due to out of comm field
            assertFailsWith<RuleGroupNotTriggeredException> {
                flush("Move")
                { +MoveCommand(guard1.facet, cid(13, 13)) }
            }

            // Try move Guard agent to cell (13,14) => must fail due to out of comm field
            assertFailsWith<RuleGroupNotTriggeredException> {
                flush("Move")
                { +MoveCommand(guard1.facet, cid(13, 14)) }
            }

            // Deploy Guard agent
            flush("Deploy")
            { +DeployCommand(guard1.facet) }
        }
    }

    fun ReteInput.createSpace() {
        for (i in 1..100) {
            for (j in 1..100) {
                val cid1 = cid(i, j)
                +Adjacent(cid1, cid(i, j + 1))
                +Adjacent(cid1, cid(i + 1, j))
            }
        }
    }

    fun ReteInput.placeAims(): Pair<ActorId, ActorId> {
        val aim1 = ActorId()
        val aim2 = ActorId()

        +Actor(aim1, pid1, ActorKind.Aim, State.Deployed)
        +Actor(aim2, pid2, ActorKind.Aim, State.Deployed)

        +At(aim1.facet, cid(10, 10))
        +At(aim2.facet, cid(90, 90))

        return Pair(aim1, aim2)
    }

    val cidPool = mutableMapOf<Int, Facet.ConstFacet<CellId>>()

    fun cid(i: Int, j: Int): Facet.ConstFacet<CellId> {
        val n = i * 1000 + j
        return cidPool.getOrPut(n) {
            CellId(n).facet as Facet.ConstFacet<CellId>
        }
    }

    val aidPool = mutableMapOf<Int, Facet<ActorId>>()

    fun aid(i: Int): Facet<ActorId> {
        return aidPool.getOrPut(i) {
            ActorId(i).facet
        }
    }

}

fun main(args: Array<String>) {
    System.out.print(">>")
    System.`in`.read()
    GameScenarioTest().main()
    System.out.print(">>>")
    System.`in`.read()
}