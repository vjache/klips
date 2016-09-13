package org.klips.engine.rete

import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.engine.*
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.StrategyOneMem
import org.klips.engine.rete.builder.Trigger
import org.jgrapht.DirectedGraph
import org.jgrapht.graph.SimpleDirectedGraph


class ReteTestGraphBuilder {

    val pattern1 = mutableSetOf<Fact>().apply {
        add(Player(ref("pid"), ref("color")))
        add(Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")))
    }

    val pattern2 = mutableSetOf<Fact>().apply {
        add(Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")) )
        add(Land(ref("cid"), ref("land")))
        add(At(ref("aid"), ref("cid")))
    }

    val pattern3 = mutableSetOf<Fact>().apply {
        add(Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")))
        add(At(ref("aid"), ref("cid")))
        add(Resource(ref("cid"), ref("type"), ref("amount")))
    }

    val pattern4 = mutableSetOf<Fact>().apply {
        add(Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")))
        add(At(ref("aid"), ref("cid")))
        add(Actor(ref("aid1"), ref("pid1"), ref("kind1"), ref("nrgy1"), ref("hlth1"), ref("state1")))
        add(At(ref("aid1"), ref("cid1")))
        add(Adjacent(ref("cid"), ref("cid1")))
    }

    val stdTrigger = object : Trigger {
        override fun fire(cache: MutableMap<Any, Any>,solution: Modification<Binding>, addEffect: (Modification<Fact>) -> Unit) {
            println(solution)
        }

    }

    fun create(): DirectedGraph<Node, MyEdge<Node, Node, String>> {
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

        val allNodes = StrategyOneMem(mutableListOf<RuleClause>().apply {
            add(RuleClause(pattern4, stdTrigger))
            add(RuleClause(pattern3, stdTrigger))
            add(RuleClause(pattern2, stdTrigger))
            add(RuleClause(pattern1, stdTrigger))

//            add(Pair(pattern4, stdTrigger))
        }).allNodes



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