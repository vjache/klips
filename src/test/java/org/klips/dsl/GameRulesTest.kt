package org.klips.dsl

import org.junit.Test
import org.klips.ReferenceNotBoundException
import org.klips.engine.*
import org.klips.engine.ActorKind.Aim
import org.klips.engine.State.Deployed
import org.klips.engine.State.OnMarch
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GameRulesTest {

    @Test
    fun createCommand() {
        GameRules().input.flush("CreateAgent") {
            +Adjacent(2, 1)
            +At(100, 1)
            +Actor(100, 1000, Aim, 100f, 100f, Deployed)
            +CreateAgentCommand(ActorId(100), CellId(2), ActorKind.Comm)
        }
        assertNotNull(ActorId.last)
    }

    @Test
    fun moveCommand() {
        GameRules().input.flush("Move") {
            +Adjacent(2, 1)
            +At(100, 1)
            +Actor(100, 1000, Aim, 7f, 100f, OnMarch)
            +MoveCommand(ActorId(100).facet, CellId(2).facet)
            +CommField(ActorId(111).facet, CellId(2).facet)
        }
    }

    @Test
    fun attackCommand() {
        GameRules().input.flush("Attack") {
            +AttackCommand(ActorId(100).facet, ActorId(101).facet)
            +At(ActorId(100).facet, CellId(1).facet)
            +Adjacent(CellId(1).facet, CellId(2).facet)
            +At(ActorId(101).facet, CellId(2).facet)
            +Actor(100, 1000, ActorKind.Guard, 10f, 100f, OnMarch)
            +Actor(101, 1001, ActorKind.Comm, 10f, 100f, Deployed)
        }
    }

    @Test
    fun deployCommand() {
        GameRules().input.flush("Deploy") {
            +DeployCommand(ActorId(100).facet)
            +Actor(100, 1000, ActorKind.Guard, 10f, 100f, OnMarch)
        }
    }

    @Test
    fun undeployCommand() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("Undeploy") {
                +UndeployCommand(ActorId(100).facet)
                +Actor(100, 1000, ActorKind.Guard, 10f, 100f, Deployed)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun chargeCommand() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("Charge") {
                +ChargeCommand(ActorId(100).facet, ActorId(101).facet)
                +At(ActorId(100).facet, CellId(1).facet)
                +Adjacent(CellId(1).facet, CellId(2).facet)
                +At(ActorId(101).facet, CellId(2).facet)
                +Actor(100, 1000, ActorKind.Solar, 10f, 100f, OnMarch)
                +Actor(101, 1000, ActorKind.Comm, 10f, 100f, Deployed)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun repairCommand() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("Repair") {
                +RepairCommand(ActorId(100).facet, ActorId(101).facet)
                +At(ActorId(100).facet, CellId(1).facet)
                +Adjacent(CellId(1).facet, CellId(2).facet)
                +At(ActorId(101).facet, CellId(2).facet)
                +Actor(100, 1000, ActorKind.Worker, 10f, 100f, OnMarch)
                +Actor(101, 1000, ActorKind.Comm, 10f, 100f, Deployed)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun scrapCommand() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("Scrap") {
                +ScrapCommand(ActorId(100).facet, ActorId(101).facet)
                +At(ActorId(100).facet, CellId(1).facet)
                +Adjacent(CellId(1).facet, CellId(2).facet)
                +At(ActorId(101).facet, CellId(2).facet)
                +Actor(100, 1000, ActorKind.Aim, 10f, 100f, Deployed)
                +Actor(101, 1000, ActorKind.Guard, 10f, 100f, OnMarch)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun feedAimCommand() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("FeedAim") {
                +FeedAimCommand(ActorId(100).facet, ActorId(101).facet)
                +At(ActorId(100).facet, CellId(1).facet)
                +Adjacent(CellId(1).facet, CellId(2).facet)
                +At(ActorId(101).facet, CellId(2).facet)
                +Actor(100, 1000, ActorKind.Worker, 10f, 100f, OnMarch)
                +Actor(101, 1000, ActorKind.Aim, 10f, 100f, Deployed)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun passiveSolarChargeOnNewTurn() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("TurnStart.Solar.Charge") {
                +Turn(PlayerId(1000).facet)
                +Actor(101, 1000, ActorKind.Solar, 10f, 100f, Deployed)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun commFieldOnMarche() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("Comm.Field.OnMarche") {
                +Actor(100, 1000, ActorKind.Comm, 10f, 100f, OnMarch)
                +At(100,1)
                +Adjacent(1, 2)
                +Adjacent(1, 3)
                +Adjacent(1, 4)
                +Adjacent(1, 5)
                +Adjacent(1, 6)
                +Adjacent(1, 7)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun commFieldDeployed() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("Comm.Field.Deployed") {
                +Actor(100, 1000, ActorKind.Comm, 10f, 100f, Deployed)
                +At(100,1)
                +Adjacent(1, 2)
                +Adjacent(2, 3)
                +Adjacent(1, 4)
                +Adjacent(4, 5)
                +Adjacent(1, 6)
                +Adjacent(6, 7)
            }
            gameRules.rete!!.printSummary()
        } catch (e: Exception) {
            gameRules.rete!!.printSummary()
            throw e
        }
    }

    @Test
    fun guardHealthInterchange() {
        val gameRules = GameRules()
        try {
            gameRules.input.flush("Guards.Health.Interchange") {
                +Turn(PlayerId(1000).facet)
                +Actor(100, 1000, ActorKind.Guard, 10f, 20f, Deployed)
                +Actor(101, 1000, ActorKind.Guard, 10f, 100f, Deployed)
                +At(100,1)
                +At(101,2)
                +Adjacent(1, 2)
            }
            gameRules.printSummary()
        } catch (e: Exception) {
            gameRules.printSummary()
            throw e
        }
    }

    @Test
    fun refNotBound() {
        assertFailsWith<ReferenceNotBoundException> {
            val gameRules = GameRules()
            gameRules.input.flush("RefNotBound.Exception") {
                +Actor(171717, 1000, ActorKind.Guard, 10f, 20f, Deployed)
            }
        }
    }

    @Test
    fun ruleConcurrency() {
        val g = GameRules()
        g.input.flush() {
            +Actor(151515, 1000, ActorKind.Guard, 10f, 20f, Deployed)
            +MoveCommand(ActorId(151515).facet, CellId(101010).facet)
            +AttackCommand(ActorId(151515).facet, ActorId(121212).facet)
        }
        assertEquals(1, g.triggered.size)
    }

    @Test
    fun ruleConcurrency2() {
        val g = GameRules()
        g.input.flush() {
            +Actor(161616, 109109109, ActorKind.Guard, 10f, 20f, Deployed)
            +RepairCommand(ActorId(161616).facet, ActorId(161617).facet)
            +RepairCommand(ActorId(161616).facet, ActorId(161618).facet)
            +RepairCommand(ActorId(161616).facet, ActorId(161619).facet)
            +RepairCommand(ActorId(161616).facet, ActorId(161620).facet)
            +RepairCommand(ActorId(161616).facet, ActorId(161621).facet)
            +RepairCommand(ActorId(161616).facet, ActorId(161622).facet)
        }
        assertEquals(1, g.triggered.size)
    }
}