package org.klips.engine.rete

import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.query.*
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.mem.StrategyOneMem
import org.klips.engine.rete.builder.Trigger
import org.jgrapht.DirectedGraph
import org.jgrapht.graph.SimpleDirectedGraph


object ReteGraphBuilder {


    fun create(reteBuilder : ReteBuilderStrategy): DirectedGraph<Node, MyEdge<Node, Node, String>> {
        val g = SimpleDirectedGraph<Node, MyEdge<Node, Node, String>> {
            n1, n2 ->
            when (n1) {
                is BetaNode -> {
                    MyEdge(n1, n2, when (n2) {
                        n1.left -> "left"
                        n1.right -> "right"
                        else -> throw IllegalArgumentException()
                    })
                }
                is ProxyNode -> {
                    MyEdge(n1, n2, "renames")
                }
                else -> throw IllegalArgumentException()
            }
        }

        val allNodes = reteBuilder.allNodes



        fun addRecursive(n:Node){
            if (n !in g.vertexSet()) g.addVertex(n)
            when (n)
            {
                is BetaNode -> {
                    addRecursive(n.left)
                    addRecursive(n.right)
                    g.addEdge(n, n.left)
                    g.addEdge(n, n.right)
                }
                is ProxyNode ->
                {
                    addRecursive(n.node)
                    g.addEdge(n, n.node)
                }
            }
        }

        allNodes.forEach { n ->
            addRecursive(n)
        }

        return g
    }

    fun <T : Comparable<T>> ref(id:String) = FacetRef<T>(id)
}