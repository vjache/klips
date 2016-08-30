package org.klips.dsl

import org.klips.engine.*
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class RuleDslTest {

    @Test
    fun onMoveTest() {
        val r = TapRules()
        r.input.flush {
            assert(Adjacent(0, 1),
                    At(100, 1),
                    Actor(100, 1000, ActorKind.Aim, 7f, 100f, State.OnMarch),
                    ActorSelected(100))
        }.blink(TapCell(0))

        assertTrue("Move must happen.") { r.onMove }

        r.onMove = false

        r.input.blink(TapCell(0))

        assertTrue("Move must happen again.") { r.onMove }

        r.onMove = false

        r.input.blink(TapCell(0))

        assertFalse("Move must NOT happen again.") { r.onMove }

        r.onMove = false

        r.input.blink(TapCell(0))

        assertFalse("Move must NOT happen again.") { r.onMove }
    }

    @Test
    fun onMoveTest2() {
        val r = TapRules()
        r.input.assert(
                Adjacent(1, 0),
                At(100, 1),
                Actor(100, 1000, ActorKind.Aim),
                ActorSelected(100)
        ).blink(TapCell(0))

        assertTrue("Move must happen.") { r.onMove }
    }

    @Test
    fun onAttackTest() {
        val r = TapRules()
        r.input.assert(
                Actor(100, 1000, ActorKind.Aim),
                At(100, 0),
                Adjacent(0, 1),
                At(101, 1),
                Actor(101, 1001, ActorKind.Comm),
                ActorSelected(101)
        ).blink(TapActor(100))

        assertTrue("Attack must happen.") { r.onAttack }
    }

    @Test
    fun onAttackTest2() {
        val r = TapRules()
        r.input.assert(
                Actor(100, 1000, ActorKind.Aim),
                At(100, 0),
                Adjacent(1, 0),
                At(101, 1),
                Actor(101, 1001, ActorKind.Comm),
                ActorSelected(101)
        ).blink(TapActor(100))

        assertTrue("Attack must happen.") { r.onAttack }
    }
}