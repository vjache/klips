package org.klips.engine.query

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Facet.IntFacet
import org.klips.dsl.Fact
import org.klips.dsl.ref
import org.klips.dsl.substitute
import org.klips.engine.PatternMatcher
import kotlin.test.assertEquals


class FactBindingSetTest {

    class Adjacent(val aid1: Facet<Int> = ref(), val aid2: Facet<Int> = ref()) : Fact(){
        constructor(a1: Int, a2:Int ):this(IntFacet(a1), IntFacet(a2))
    }

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {

    }

    @Test
    fun basicSubst(){
        val patt1 = Adjacent(FacetRef<Int>("x"), FacetRef<Int>("y"))
        val patt2 = Adjacent(FacetRef<Int>("aid2"), FacetRef<Int>("aid1"))
        val patt2c = patt2.substitute(FacetRef<Int>("aid2"), FacetRef<Int>("xxx2"))

        assertEquals(Adjacent(ref("xxx2"), ref("aid1")), patt2c)
        println(patt2)
        println(patt2c)

        println("Before subst: $patt2")
        println("After subst: ${patt2.substitute(
                PatternMatcher(patt2).bind(patt1)!!)}")

        assertEquals(
                patt1,
                patt2.substitute(
                        PatternMatcher(patt2).bind(patt1)!!))
    }

    @Test
    fun onlyMatchingBindings() {
        val facts = listOf(
                Adjacent(0, 1),
                Adjacent(0, 2),
                Adjacent(0, 3),
                Adjacent(1, 1),
                Adjacent(1, 2),
                Adjacent(1, 3),
                Adjacent(IntFacet(0), FacetRef<Int>("x"))
        )

        val bs = FactBindingSet(
                Adjacent(IntFacet(0), FacetRef<Int>("aid1")),
                facts)
        assert(bs.refs.size == 1)
        assert(bs.size == 4)
        println(bs)
    }

    fun <T : Comparable<T>> ref(id:String) = Facet.FacetRef<T>(id)
}