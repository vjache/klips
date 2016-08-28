package org.klips.engine.graph

import org.jgrapht.EdgeFactory
import org.jgrapht.UndirectedGraph
import org.jgrapht.WeightedGraph
import org.jgrapht.graph.AsWeightedGraph
import org.jgrapht.graph.GraphDelegator
import org.jgrapht.graph.SimpleGraph

class AutoGraph<V, E>(g: UndirectedGraph<V, E>, weights:MutableMap<E,Double>) :
        GraphDelegator<V, E>(AsWeightedGraph(g, weights)),
        UndirectedGraph<V, E>,
        WeightedGraph<V, E> {

    constructor(g: UndirectedGraph<V, E>) : this(g, mutableMapOf())

    constructor(ef:EdgeFactory<V, E>) : this(SimpleGraph(ef))

    constructor(weights:MutableMap<E, Double>, ef:EdgeFactory<V, E>) : this(SimpleGraph(ef), weights)

    override fun addVertex(v: V): Boolean {
        val b = super.addVertex(v)
        vertexSet().forEach {
            if(it != v) {
                val e = edgeFactory.createEdge(it, v)
                if(e != null) {
                    if (e !in edgeSet())
                        addEdge(it, v, e)
                    setEdgeWeight(e, getEdgeWeight(e) * 0.9)
                }
            }
        }
        return b
    }
}