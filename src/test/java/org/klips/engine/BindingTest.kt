package org.klips.engine

import org.junit.Test
import org.klips.dsl.facet
import org.klips.dsl.ref
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BindingTest {

    @Test
    fun composeBindingTest() {
        val b = ComposeBinding(
                SimpleBinding(
                        ref<Int>("x") to 1.facet,
                        ref<Int>("y") to 2.facet),
                SimpleBinding(
                        ref<Int>("x") to 1.facet,
                        ref<Int>("z") to 3.facet)
        )

        assert(b.size == 3)
        assert(b.keys.size == 3)
        assert(b.values.size == 3)
        assert(b.entries.size == 3)
        assertEquals(1.facet, b[ref<Int>("x")])
        assertEquals(2.facet, b[ref<Int>("y")])
        assertEquals(3.facet, b[ref<Int>("z")])
        val i = b.iterator()
        assert(i.hasNext())
        i.next()
        assert(i.hasNext())
        i.next()
        assert(i.hasNext())
        i.next()
        assert(!i.hasNext())
    }

    @Test
    fun composeBindingComplexTest() {
        val l1 = SimpleBinding(
                ref<Int>("x") to 1.facet,
                ref<Int>("y") to 2.facet)
        val r1 = SimpleBinding(
                ref<Int>("x") to 1.facet,
                ref<Int>("z") to 3.facet)
        val b1 = ComposeBinding(l1, r1)

        val l2 = SimpleBinding(
                ref<Int>("x") to 1.facet,
                ref<Int>("y") to 2.facet,
                ref<Int>("u") to 2.facet)
        val r2 = SimpleBinding(
                ref<Int>("x") to 1.facet,
                ref<Int>("v") to 4.facet,
                ref<Int>("w") to 3.facet)
        val b2 = ComposeBinding(
                l2,
                r2
        )
        val b = ComposeBinding(b1, b2)

        assertEquals(
                r1.keys union r1.keys union l2.keys union r2.keys,
                b.keys)

        assertEquals(6,b.size)
        assertEquals(6,b.keys.size)
        assertEquals(6,b.values.size)
        assertEquals(6, b.entries.size)
        assertEquals(1.facet, b[ref<Int>("x")])
        assertEquals(2.facet, b[ref<Int>("y")])
        assertEquals(3.facet, b[ref<Int>("z")])
        assertEquals(4.facet, b[ref<Int>("v")])
        assertEquals(3.facet, b[ref<Int>("w")])
        val i = b.iterator()
        assert(i.hasNext())
        i.next() // 1
        assert(i.hasNext())
        i.next() // 2
        assert(i.hasNext())
        i.next() // 3
        assert(i.hasNext())
        i.next() // 4
        assert(i.hasNext())
        i.next() // 5
        assert(i.hasNext())
        i.next() // 6
        assert(!i.hasNext())
    }

    @Test
    fun factBindingTest() {
        val b = FactBinding(Adjacent(CellId(0).facet, CellId(1).facet),
                mapOf(ref<CellId>("aid1") to 0, ref<CellId>("aid2") to 1))
        assert(b.size == 2)
        assert(b.keys.size == 2)
        assert(b.values.size == 2)
        assert(b.entries.size == 2)
        assertEquals(CellId(0).facet, b[ref<CellId>("aid1")])
        assertEquals(CellId(1).facet, b[ref<CellId>("aid2")])
        assertNull(b[ref<CellId>("aidX")])
        val i = b.iterator()
        assert(i.hasNext())
        i.next()
        assert(i.hasNext())
        i.next()
        assert(!i.hasNext())
    }

    @Test
    fun factBindingTest2() {
        val b = FactBinding(Adjacent(CellId(0).facet, CellId(0).facet),
                mapOf(ref<CellId>("aid1") to 0, ref<CellId>("aid1") to 1))
        assert(b.size == 1)
        assert(b.keys.size == 1)
        assert(b.values.size == 1)
        assert(b.entries.size == 1)
        assertEquals(CellId(0).facet, b[ref<CellId>("aid1")])
        assertNull(b[ref<CellId>("aidX")])
        val i = b.iterator()
        assert(i.hasNext())
        i.next()
        assert(!i.hasNext())
    }

    @Test
    fun projectBindingTest() {
        val b0 = SimpleBinding(
                ref<Int>("v") to 0.facet,
                ref<Int>("w") to 1.facet,
                ref<Int>("x") to 2.facet,
                ref<Int>("y") to 3.facet,
                ref<Int>("z") to 4.facet)
        val b = ProjectBinding(
                setOf(ref<Int>("x")),
                b0)
        assert(b.size == 1)
        assert(b.keys.size == 1)
        assert(b.values.size == 1)
        assert(b.entries.size == 1)
        assertEquals(2.facet, b[ref<Int>("x")])
        assertNotNull(b0[ref<Int>("v")])
        assertNull(b[ref<Int>("v")])
        assertNotNull(b0[ref<Int>("w")])
        assertNull(b[ref<Int>("w")])
        assertNotNull(b0[ref<Int>("y")])
        assertNull(b[ref<Int>("y")])
        assertNotNull(b0[ref<Int>("z")])
        assertNull(b[ref<Int>("z")])

        val i = b.iterator()
        assert(i.hasNext())
        i.next()
        assert(!i.hasNext())
    }

    @Test
    fun simpleBindingTest() {
        val b = SimpleBinding(
                ref<Int>("v") to 0.facet,
                ref<Int>("w") to 1.facet,
                ref<Int>("x") to 2.facet,
                ref<Int>("y") to 3.facet,
                ref<Int>("z") to 4.facet)
        assert(b.size == 5)
        assert(b.keys.size == 5)
        assert(b.values.size == 5)
        assert(b.entries.size == 5)
        assertEquals(0.facet, b[ref<Int>("v")])
        assertEquals(1.facet, b[ref<Int>("w")])
        assertEquals(2.facet, b[ref<Int>("x")])
        assertEquals(3.facet, b[ref<Int>("y")])
        assertEquals(4.facet, b[ref<Int>("z")])

        val i = b.iterator()
        assert(i.hasNext())
        i.next() // 1
        assert(i.hasNext())
        i.next() // 2
        assert(i.hasNext())
        i.next() // 3
        assert(i.hasNext())
        i.next() // 4
        assert(i.hasNext())
        i.next() // 5
        assert(!i.hasNext())
    }

}