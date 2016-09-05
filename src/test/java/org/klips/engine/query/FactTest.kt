package org.klips.engine.query

import org.klips.dsl.Facet
import org.junit.Test
import org.klips.dsl.facet
import org.klips.engine.*
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals


class FactTest {

    @Test
    fun patternEqualities(){
        assertEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")),
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")))

        assertEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")).hashCode(),
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")).hashCode())

        assertNotEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")),
                Actor(ref("aid"), ref("pid"), ref("kind-1"), ref("nrgy"), ref("hlth"), ref("state")))

        assertNotEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")).hashCode(),
                Actor(ref("aid"), ref("pid"), ref("kind-1"), ref("nrgy"), ref("hlth"), ref("state")).hashCode())
    }

    @Test
    fun factsEqualities(){
        assertEquals(
                Actor(1, 1, ActorKind.Comm),
                Actor(1, 1, ActorKind.Comm))

        assertEquals(
                Actor(1, 1, ActorKind.Comm).hashCode(),
                Actor(1, 1, ActorKind.Comm).hashCode())

        assertNotEquals(
                Actor(1, 1, ActorKind.Comm),
                Actor(1, 1, ActorKind.Aim))

        assertNotEquals(
                Actor(1, 1, ActorKind.Comm).hashCode(),
                Actor(1, 1, ActorKind.Aim).hashCode())
    }

    @Test
    fun factsFieldsDelegation() {
        val a = Actor(1, 1, ActorKind.Comm)
        assertEquals(ActorId(1).facet, a.aid)
        assertEquals(PlayerId(1).facet, a.pid)
        assertEquals(ActorKind.Comm.facet, a.type)
        assertEquals(Level(100f).facet, a.energy)

        val a1 = a.substitute(ActorId(1).facet, ActorId(2).facet) as Actor

        assertEquals(ActorId(2).facet, a1.aid)
        assertEquals(PlayerId(1).facet, a1.pid)


//        assertFails { a.energy }
    }

    fun <T : Comparable<T>> ref(id:String) = Facet.FacetRef<T>(id)
}