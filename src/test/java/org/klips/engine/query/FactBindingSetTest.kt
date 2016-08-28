package org.klips.engine.query

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Facet.IntFacet
import org.klips.dsl.Fact
import org.klips.engine.PatternMatcher
import org.junit.After
import org.junit.Before
import org.junit.Test


class FactBindingSetTest {

    class Adjacent(aid1: Facet<Int>, aid2: Facet<Int>) : Fact(aid1, aid2){
        constructor(a1: Int, a2:Int ):this(IntFacet(a1), IntFacet(a2))
    }

    @Before
    fun setUp() {

    }

    @After
    fun tearDown() {

    }

    @Test
    fun basicMatcher(){
        val facts1 = arrayOf(
                Adjacent(0, 1),
                Adjacent(0, 2),
                Adjacent(1, 1)
                )
        val patt1 = PatternMatcher(Adjacent(IntFacet(0), FacetRef<Int>("aid1")))

        facts1.forEach { println(patt1.bind(it)) }
    }

    @Test
    fun sameMatcher(){
        val facts1 = arrayOf(
                Adjacent(0, 1),
                Adjacent(0, 2),
                Adjacent(1, 1),
                Adjacent(FacetRef<Int>("id1"), FacetRef<Int>("id1")),
                Adjacent(FacetRef<Int>("id1"), FacetRef<Int>("id2"))
        )
        val patt1 = PatternMatcher(Adjacent(FacetRef<Int>("aid1"), FacetRef<Int>("aid1")))

        facts1.forEach { println(patt1.bind(it)) }

        val patt2 = PatternMatcher(Adjacent(FacetRef<Int>("x"), FacetRef<Int>("y")))

        facts1.forEach { println(patt2.bind(it)) }
    }
    @Test
    fun basicSubst(){
        val patt1 = Adjacent(FacetRef<Int>("x"), FacetRef<Int>("y"))
        val patt2 = Adjacent(FacetRef<Int>("aid2"), FacetRef<Int>("aid1"))
        val patt2c = patt2.substitute(FacetRef<Int>("aid2"), FacetRef<Int>("xxx2"))

        println(patt2)
        println(patt2c)

        println("Before subst: $patt2")
        println("After subst: ${patt2.substitute(
                PatternMatcher(patt2).bind(patt1)!!)}")
    }

    @Test
    fun basicMatch() {
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
}