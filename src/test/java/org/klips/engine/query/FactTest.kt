package org.klips.engine.query

import org.klips.dsl.Facet
import org.klips.engine.Actor
import org.klips.engine.ActorKind
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class FactTest {

    @Test
    fun patternEqualities(){
        assertEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy")),
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy")))

        assertEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy")).hashCode(),
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy")).hashCode())

        assertNotEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy")),
                Actor(ref("aid"), ref("pid"), ref("kind-1"), ref("nrgy")))

        assertNotEquals(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy")).hashCode(),
                Actor(ref("aid"), ref("pid"), ref("kind-1"), ref("nrgy")).hashCode())
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

    fun <T : Comparable<T>> ref(id:String) = Facet.FacetRef<T>(id)
}