package org.klips.dsl

import org.junit.Test
import org.klips.engine.*
import org.klips.engine.ActorKind.Aim
import org.klips.engine.State.Deployed
import org.klips.engine.State.OnMarch

/**
 * Created by vj on 08.09.16.
 */
class GameRulesTest {
    @Test
    fun moveCommand() {
        GameRules().input.flush("Move") {
            +Adjacent(2, 1)
            +At(100, 1)
            +Actor(100, 1000, Aim, 7f, 100f, OnMarch)
            +MoveCommand(ActorId(100).facet, CellId(2).facet)
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
}