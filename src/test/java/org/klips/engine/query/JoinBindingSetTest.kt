package org.klips.engine.query

import org.klips.dsl.Facet.ConstFacet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.engine.*
import org.klips.engine.query.JoinBindingSet.JoinType.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.After


class JoinBindingSetTest {

    val world = TestWorld(10)

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {

    }

    @Test
    fun testFactBindingSet_Adjacent() {
        val fbs = FactBindingSet(
                Adjacent(FacetRef<CellId>("cid1"), FacetRef<CellId>("cid2")),
                world.facts)

        assertEquals(fbs.toSet().size, fbs.size)
        assertEquals(2 * (world.max - 1) * world.max, fbs.size)
    }

    @Test
    fun testFactBindingSet_Land() {
        val fbs = FactBindingSet(
                Land(FacetRef<CellId>("cid"), FacetRef<LandKind>("type")),
                world.facts)

        assertEquals(fbs.toSet().size, fbs.size)
        assertEquals(world.max * world.max, fbs.size)
    }

    @Test
    fun testFactBindingSet_Player() {
        val fbs = FactBindingSet(
                Player(ref("pid"), ref("color")),
                world.facts)

        assertEquals(fbs.toSet().size, fbs.size)
        assertEquals(4, fbs.size)
    }

    @Test
    fun testFactBindingSet_Actor() {
        val fbs = FactBindingSet(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")),
                world.facts)

        assertEquals(fbs.toSet().size, fbs.size)
        assertEquals(9, fbs.size)
    }

    fun testFactBindingSet_Actor(pid: ConstFacet<PlayerId>) {
        if(pid.value.id == 4)
            return

        val fbs = FactBindingSet(
                Actor(ref("aid"),
                        pid,
                        ref("kind"),
                        ref("nrgy"), ref("hlth"), ref("state")),
                world.facts)

        assertEquals(fbs.toSet().size, fbs.size)
        assertEquals(3, fbs.size)
    }

    @Test
    fun testFactBindingSet_ActorsPerPlayer() {
        val pid = FacetRef<PlayerId>("pid")

        val fbs = FactBindingSet(
                Player(pid, FacetRef<PlayerColor>("color")),
                world.facts)

        for(b in fbs)
            testFactBindingSet_Actor(b.fetch(pid))

        assertEquals(4, fbs.size)
    }

    @Test
    fun testProd(){
        val l0 = emptyList<Int>()
        val l1 = listOf(1)
        val l2 = listOf(4,5,6)

        assertEquals(l2.size, ProductIterable(l1, l2).count())

        assertEquals(0, ProductIterable(l0, l2).count())
        assertEquals(0, ProductIterable(l0, l0).count())
        assertEquals(0, ProductIterable(l2, l0).count())

        assertEquals(l2.size * l2.size, ProductIterable(l2, l2).count())

        assertEquals(1, ProductIterable(l1, l1).count())
    }

    @Test
    fun testJoin_PlayersAndActors() {

        val bsPlayers = FactBindingSet(
                Player(FacetRef<PlayerId>("pid"), FacetRef<PlayerColor>("color")),
                world.facts)

        val bsActors = FactBindingSet(
                Actor(ref("aid"),
                      ref("pid"),
                      ref("kind"),
                      ref("nrgy"), ref("hlth"), ref("state")),
                world.facts)

        val bsJoin      = JoinBindingSet(bsPlayers, bsActors)
        val bsJoinFull  = JoinBindingSet(bsPlayers, bsActors, FullOuter)
        val bsJoinRight = JoinBindingSet(bsPlayers, bsActors, RightOuter)
        val bsJoinLeft  = JoinBindingSet(bsPlayers, bsActors, LeftOuter)

        assertEquals(9,  bsJoin.size)
        assertEquals(10, bsJoinFull.size)
        assertEquals(9,  bsJoinRight.size)
        assertEquals(10, bsJoinLeft.size)

        println(bsJoinFull)

        assert(bsJoin.first().refs.size > 0)
        assert( bsPlayers.refs.toSet().union(bsActors.refs.toSet()).containsAll(
                bsJoin.first().refs.toSet()))
    }

    @Test
    fun testJoin_ActorsAndLands() {

        val bsActors = FactBindingSet(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")),
                world.facts)

        val bsLands = FactBindingSet(
                Land(ref("cid"), ref("land")),
                world.facts)

        val bsAts = FactBindingSet(
                At(ref("aid"), ref("cid")),
                world.facts)

        val bsJoin = JoinBindingSet(
                bsActors,
                JoinBindingSet(bsAts,bsLands))

        assertEquals(9, bsJoin.size)

    }

    @Test
    fun testJoin_ActorsAndResources() {

        val actorKind = FacetRef<ActorKind>("kind")
        val bsActors = FactBindingSet(
                Actor(ref("aid"), ref("pid"), actorKind, ref("nrgy"), ref("hlth"), ref("state")),
                world.facts)

        val bsAts = FactBindingSet(
                At(ref("aid"), ref("cid")),
                world.facts)

        val bsResources = FactBindingSet(
                Resource(ref("cid"), ref("type"), ref("amount")),
                world.facts)

        val bsJoin = JoinBindingSet(
                bsActors,
                JoinBindingSet(
                bsResources, bsAts))

        assertEquals(9, bsJoin.size)

        val bsCommActors = FactBindingSet(
                Actor(  ref("aid"), ref("pid"),
                        ConstFacet(ActorKind.Comm),
                        ref("nrgy"), ref("hlth"), ref("state")),
                world.facts)

        val bsJoin2 = JoinBindingSet(
                bsCommActors,
                JoinBindingSet(bsResources, bsAts))


        assertEquals(3, bsJoin2.size)

        val bsJoin3 = JoinBindingSet(bsJoin2, bsActors)

        assert(bsJoin3.all { it.fetchValue<ActorKind>(actorKind) == ActorKind.Comm})

    }

    @Test
    fun testJoin_Patterns() {

        val set = mutableSetOf<Fact>().apply {
            for(i in 1..3) add(Player(ref("pid-$i"), ref("color-$i")))
            for(i in 1..3) add(Actor(ref("aid-$i"), ref("pid-$i"), ref("kind-$i"), ref("nrgy-$i"), ref("hlth-$i"), ref("state-$i")))
        }

        val bsPlayers = FactBindingSet(
                Player(ref("pid"), ref("color")), set)

        val bsActors = FactBindingSet(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")), set)

        val bsJoin = JoinBindingSet(bsPlayers, bsActors)

        println(bsJoin)

        assertEquals(3, bsJoin.size)

    }

    fun <T : Comparable<T>> ref(id:String) = FacetRef<T>(id)
}