@file:Suppress("UNCHECKED_CAST")

package org.klips.engine.rete

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Facet.IntFacet
import org.klips.dsl.Fact
import org.klips.engine.graph.AutoGraph
import org.jgrapht.GraphMapping
import org.jgrapht.alg.ConnectivityInspector
import org.jgrapht.alg.PrimMinimumSpanningTree
import org.jgrapht.alg.interfaces.MinimumSpanningTree
import org.jgrapht.alg.isomorphism.IsomorphismInspector
import org.jgrapht.alg.isomorphism.VF2GraphIsomorphismInspector
import org.jgrapht.graph.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.klips.dsl.ref

class JGraphTTest {

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
    fun basicMatch() {
        val g = AutoGraph(SimpleGraph<Fact, Set<FacetRef<*>>> {
            f1, f2 ->
            val s = f1.facets.filter { it is FacetRef<*> }.intersect(
                    f2.facets.filter { it is FacetRef<*> }
            )
            if (s.size == 0) null
            else s as Set<FacetRef<*>>
        })

        with(g){
            val cellId1 = FacetRef<Int>("cellId1")
            val cellId2 = FacetRef<Int>("cellId2")
            val cellId3 = FacetRef<Int>("cellId3")
            val v1 = Adjacent(cellId1, IntFacet(0))
            val v2 = Adjacent(cellId1, cellId2)
            val v3 = Adjacent(IntFacet(1), cellId2)
            val v4 = Adjacent(IntFacet(1), cellId3)

            addVertex(v1)
            addVertex(v2)
            addVertex(v3)
            addVertex(v4)

            //addEdge(v1, v2)
        }
        System.out.println("$g")
        val ci = ConnectivityInspector(g)
        System.out.println("${ci.connectedSets()}")
    }

    @Test
    fun mstTest(){
        val g = AsWeightedGraph(
                SimpleGraph<Int, DefaultEdge>(DefaultWeightedEdge::class.java),
                mutableMapOf())
        val iso: IsomorphismInspector<Int, DefaultEdge> = VF2GraphIsomorphismInspector(g,g)

        iso.isomorphismExists()

        iso.mappings
//        while(iter.)
//        {
//
//        }
        with(g){
            addVertex(1)
            addVertex(2)
            addVertex(3)
            addVertex(4)
            addVertex(5)

            setEdgeWeight(addEdge(1,2), 2.0)
            setEdgeWeight(addEdge(2,3), 1.0)
            setEdgeWeight(addEdge(3,4), 1.0)
            setEdgeWeight(addEdge(4,1), 2.0)

            setEdgeWeight(addEdge(1,3), 1.0)
            setEdgeWeight(addEdge(2,4), 2.0)
            setEdgeWeight(addEdge(3,5), 1.0)
        }

        val mst = PrimMinimumSpanningTree(g)

        println(mst.minimumSpanningTreeEdgeSet)
    }

}