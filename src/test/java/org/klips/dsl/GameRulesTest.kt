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
}