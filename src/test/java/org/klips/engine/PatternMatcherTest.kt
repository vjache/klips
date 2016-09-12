package org.klips.engine

import org.junit.Test
import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.dsl.facet
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class PatternMatcherTest {

    val aid = ref<ActorId>("aid")
    val kind = ref<ActorKind>("kind")
    val pid = ref<PlayerId>("pid")
    val nrgy = ref<Level>("nrgy")
    val hlth = ref<Level>("hlth")
    val state = ref<State>("state")

    @Test
    fun asymmetricBind() {
        val fact1 = Actor(aid, pid, kind, nrgy, hlth, Facet.ConstFacet(State.OnMarch))
        val fact2 = Actor(aid, pid, kind, nrgy, hlth, state)
        val b1 = PatternMatcher(fact2).bind(fact1)
        assertNotNull(b1)
        println("$b1")
        val b2 = PatternMatcher(fact1).bind(fact2)
        assertNull(b2)
        println("$b2")
    }

    @Test
    fun bindSubclass() {

        val fact = MoveCommand(
                actingAgent = ActorId(0).facet,
                targetCell = CellId(0).facet)

        val fact2 = AttackCommand(
                actingAgent  = ActorId(10).facet,
                passiveAgentId = ActorId(1).facet)

        val pattern = UnaryCommand(aid)
        val b1 = PatternMatcher(pattern).bind(fact)
        assertNotNull(b1)
        val b2 = PatternMatcher(pattern).bind(fact2)
        assertNotNull(b2)

        assertEquals(ActorId(0).facet, b1!![aid])
        assertEquals(ActorId(10).facet, b2!![aid])

        val pattern2 = AgentToAgentCommand()
        pattern2.substitute { it }
        assertNull(PatternMatcher(pattern2).bind(fact))
        assertNotNull(PatternMatcher(pattern2).bind(fact2))

    }

    @Test
    fun bindSuperclass() {
        val pattern = MoveCommand()
        val fact = UnaryCommand(actingAgent = ActorId(0).facet)

        val b1 = PatternMatcher(pattern).bind(fact)

        assertNull(b1)
    }

    @Test
    fun basicMatcher(){
        val patt1 = PatternMatcher(Adjacent(const(CellId(0)), ref("aid1")))
        val patt2 = PatternMatcher(Adjacent(const(CellId(1)), ref("aid1")))
        val patt3 = PatternMatcher(Adjacent(const(CellId(0)), const(CellId(1))))

        assertNotNull(patt1.bind(Adjacent(0, 1)))
        assertNotNull(patt3.bind(Adjacent(0, 1)))
        assertNull(patt3.bind(patt1.pattern))
        assertNotNull(patt1.bind(Adjacent(0, 2)))
        assertNotNull(patt1.bind(patt1.pattern))
        assertNull(patt1.bind(Adjacent(1, 1)))
        assertNull(patt1.bind(patt2.pattern))

        assertEquals(const(CellId(1)), patt1.bind(Adjacent(0, 1))!![ref<Int>("aid1")])
        assertEquals(const(CellId(2)), patt1.bind(Adjacent(0, 2))!![ref<Int>("aid1")])
    }

    @Test
    fun sameMatcher(){
        val patt1 = PatternMatcher(Adjacent(ref("aid1"), ref("aid1")))

        assertNull(patt1.bind(Adjacent(0, 1)))
        assertNull(patt1.bind(Adjacent(0, 2)))
        assertNull(patt1.bind(Adjacent(ref("id1"), ref("id2"))))
        assertNotNull(patt1.bind(Adjacent(1, 1)))
        assertNotNull(patt1.bind(Adjacent(ref("id1"), ref("id1"))))

        assertEquals(ref<Int>("id1"), patt1.bind(Adjacent(ref("id1"), ref("id1")))!![ref<Int>("aid1")])

    }

    @Test
    fun absurdBinding() {
        notBindable(
                AttackCommand(ref("aid"), ref("aid1")),
                ChargeCommand(ref("aid"), ref("aid1")))

        notBindable(
                ChargeCommand(ref("aid"), ref("aid1")),
                AttackCommand(ref("aid"), ref("aid1")))

        notBindable(
                AttackCommand(ref("aid"), ref("aid1")).substitute { it },
                ChargeCommand(ref("aid"), ref("aid1")).substitute { it })

        notBindable(
                ChargeCommand(ref("aid"), ref("aid1")).substitute { it },
                AttackCommand(ref("aid"), ref("aid1")).substitute { it })
    }

    fun bindable(f1:Fact, f2:Fact) {
        assertNotNull(PatternMatcher(f1).bind(f2))
    }

    fun notBindable(f1:Fact, f2:Fact) {
        assertNull(PatternMatcher(f1).bind(f2))
    }

    fun Fact.match(f:Fact) = PatternMatcher(this).bind(f)

    fun <T : Comparable<T>> ref(id:String) = FacetRef<T>(id)
    fun <T : Comparable<T>> const(v:T) = Facet.ConstFacet(v)
}