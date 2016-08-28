package org.klips.engine.rete.builder

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.SimpleBinding
import org.klips.engine.graph.AutoGraph
import org.klips.engine.rete.*
import org.klips.engine.rete.builder.optimizer.Optimizer
import org.jgrapht.EdgeFactory
import org.jgrapht.WeightedGraph
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import java.util.*

@Suppress("UNCHECKED_CAST")
abstract class StrategyOne(patterns: List<RuleClause>) :
        ReteBuilderStrategy(patterns) {

    val agenda = PriorityQueue<Pair<Modification<Binding>, RuleClause>> { x, y ->
        x.second.priority.compareTo(y.second.priority)
    }

    override val input = object : ReteInput {

        val effectsQueue = LinkedList<Modification<out Fact>>()

        override fun flush() : ReteInput {
            // While there are effects do ...
            while (!effectsQueue.isEmpty()) {
                // 1. Apply effects to appropriate a-nodes
                while (!effectsQueue.isEmpty()) {
                    val mdf = effectsQueue.remove()
                    log(mdf)
                    // TODO: Elaborate faster a-node selection
                    for (anode in alphaLayer) {
                        if (anode.accept(mdf)) {
                            break
                        }
                    }
                }

                //2. While there are activated rule clauses in agenda ...
                while (!agenda.isEmpty()) {
                    // 2.1. Take first activation
                    val (sol, ruleClause) = agenda.remove()

                    // 2.2. Fire trigger! Do side effects and enqueue working memory modifications.
                    ruleClause.trigger.fire(sol) { mdf ->
                        effectsQueue.add(mdf)
                    }
                }

            }
            return this
        }

        override fun modify(vararg mdfs: Modification<out Fact>): ReteInput {
            effectsQueue.addAll(mdfs)
            return this
        }
    }

    override val alphaLayer: Set<AlphaNode>
    override val allNodes: Set<Node>

    init {

        val anodeByFact = mutableMapOf<Fact, AlphaNode>()
        val weights = mutableMapOf<Pair<Node, Node>, Double>()

        val workGraphs = unifiedPatterns.second.map { ruleClause ->
            // Create work graph of A/B-nodes for ruleClause
            createWorkGraph(weights).apply {
                ruleClause.pattern.forEach { fact ->
                    addVertex(anodeByFact.getOrPut(fact) {
                        createAlphaNode(fact)
                    })
                }
            }
        }

        val betaNodeRegistry = mutableMapOf<Pair<Node, Node>, BetaNode>()

        val workGraphsSorted = workGraphs.sortedBy { workGraph ->
            with(workGraph) {
                edgeSet().map { getEdgeWeight(it) }.min()
            }
        }

        do {
            var complete = workGraphsSorted.map { workGraph ->
                workGraph.reduceWorkGraph(betaNodeRegistry)
            }.all { it }
        } while (!complete)

        workGraphsSorted.forEach { workGraph ->

            if (!workGraph.edgeSet().isEmpty())
                throw IllegalStateException("Still have edges: $workGraph")

            if (workGraph.vertexSet().size != 1)
                throw IllegalStateException("Still have edges: $workGraph")
        }

        //TODO : Bind remaining rete node with trigger
        //...

        workGraphsSorted.forEachIndexed { i, workGraph ->
            val node = workGraph.vertexSet().first()
            val binding = unifiedPatterns.first[i]

            // TODO : reverse binding
            val rbinding = SimpleBinding(binding.map { Pair(it.value, it.key) })
            // TODO : make p-node pass refs unchanged when no renaming mapping
            ProxyNode(node, rbinding).addConsumer(object : Consumer {
                override fun consume(source:
                                     Node, mdf: Modification<Binding>) {
                    val entry = Pair(mdf, unifiedPatterns.second[i])
                    when (mdf) {
                    // Rule clause activated -- place it to agenda
                        is Assert -> {
                            agenda.add( log(entry, "+A ") )
                        }
                    // Rule clause deactivated -- remove it from agenda
                        is Retire -> {
                            val antiEntry = Pair(mdf.inverse(), unifiedPatterns.second[i])
                            if (!agenda.remove( antiEntry ))
                            {
                                log(entry, "+A ")
                                agenda.add(entry)
                            }
                            else
                                log(entry, "-A ")
                        }
                    }
                }
            })
        }

        val nodes0 = mutableSetOf<Node>().apply {
            workGraphs.forEach { wg -> addAll(wg.vertexSet()) }
        }
        allNodes = nodes0.subtract(Optimizer.optimize(nodes0))

        alphaLayer = mutableSetOf<AlphaNode>().apply {
            allNodes.forEach {
                it.deepTraverse {
                    if (this is AlphaNode) add(this)
                    false
                }
            }
        }
    }

    private fun WeightedGraph<Node, Pair<Node, Node>>.reduceWorkGraph(
            bnodeRegistry: MutableMap<Pair<Node, Node>, BetaNode>): Boolean {

        // End recursion
        if (edgeSet().isEmpty()) return true

        // Fetch most light edge
        val nextEdge = edgeSet().minWith(Comparator { t1, t2 ->
            getEdgeWeight(t1).compareTo(getEdgeWeight(t2))
        })!!

        removeEdge(nextEdge)

        val bnode = bnodeRegistry.getOrPut(nextEdge) {
            bnodeRegistry.getOrPut(
                    Pair(nextEdge.second, nextEdge.first)) {
                createBetaNode(nextEdge.first, nextEdge.second)
            }
        }

        setEdgeWeight(nextEdge, getEdgeWeight(nextEdge) - 0.00000001)

        removeVertex(nextEdge.first)
        removeVertex(nextEdge.second)

        // New vertex
        addVertex(bnode)

        return false
    }

    fun createWorkGraph(weights: MutableMap<Pair<Node, Node>, Double> = mutableMapOf()) =
            AutoGraph<Node, Pair<Node, Node>>(
                    weights,
                    EdgeFactory {
                        l, r ->
                        val s = l.refs.intersect(r.refs)
                        if (s.isEmpty()) null
                        else Pair(l, r)
                    })

    abstract protected fun createBetaNode(f1: Node, f2: Node): BetaNode
    abstract protected fun createAlphaNode(f1: Fact): AlphaNode

    private fun <T> log(obj: T, pfx:String = ""): T {
        when (obj) {
            is Assert<*> -> {
                println("$pfx+ ${obj.arg}")
            }
            is Retire<*> -> {
                println("$pfx- ${obj.arg}")
            }
            else -> {
                println("$pfx$obj")
            }
        }
        return obj
    }
}