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
import org.klips.RuleGroupNotTriggeredException
import org.klips.dsl.ActivationFilter.AssertOnly
import org.klips.engine.util.Log
import java.util.*


@Suppress("UNCHECKED_CAST")
abstract class StrategyOne(val agendaManager: AgendaManager, val log: Log, patterns: List<RuleClause>) :
    ReteBuilderStrategy(patterns) {

    override val input = object : ReteInput {

        val effectsQueue = LinkedList<Modification<out Fact>>()

        override fun flush(vararg expect: String): ReteInput {
            val cache = mutableMapOf<Any, Any>()
            val triggered = mutableSetOf<String>()
            var effector = "flush"
            var iterCnt = 1
            // While there are effects do ...
            do {
                log.wmEvent { "--- ${iterCnt++} apply effect of $effector ---" }
                // 1. Apply effects to appropriate a-nodes
                while (effectsQueue.isNotEmpty()) {
                    val mdf = effectsQueue.remove()
                    // TODO: Elaborate faster a-node selection
                    log.wmEvent { "${if (mdf is Assert) "+" else "-"} ${mdf.arg}" }
                    alphaLayer.forEach { it.accept(mdf) }
                }

                //2. If there is an activated rule clause in agendaManager ...
                val entry = agendaManager.next()
                entry?.let { (sol, ruleClause) ->
                    // 2.1. Fire trigger! Do side effects and enqueue working memory modifications.
                    ruleClause.trigger.fire(cache, sol) { mdf ->
                        effectsQueue.add(mdf)
                    }

                    log.wmEvent { effector = "[${ruleClause.group}:${ruleClause.priority}] ($sol)"; null }

                    if (expect.isNotEmpty())
                        triggered.add(ruleClause.group)
                }
            } while (entry != null)

            if (expect.isNotEmpty()) {
                val notTriggered = expect.filter { it !in triggered }

                if (notTriggered.isNotEmpty())
                    throw RuleGroupNotTriggeredException(notTriggered)
            }

            log.wmEvent { "--- finish ---" }
            return this
        }

        override fun modify(vararg mdfs: Modification<out Fact>): ReteInput {
            effectsQueue.addAll(mdfs)
            return this
        }
    }

    final override val alphaLayer: Set<AlphaNode>
    final override val allNodes: Set<Node>
    final override val roots: List<Pair<RuleClause, Node>>

    init {

        val anodeByFact = mutableMapOf<Fact, AlphaNode>()
        val weights = mutableMapOf<Pair<Node, Node>, Double>()

        val workGraphs = unifiedPatterns.second.mapIndexed { i, ruleClause ->
            // Create work graph of A/B-nodes for ruleClause
            val w = createWorkGraph(weights).apply {
                ruleClause.pattern.forEach { fact ->
                    addVertex(anodeByFact.getOrPut(fact) {
                        createAlphaNode(fact)
                    })
                }
            }
            Pair(i, w)
        }

        val betaNodeRegistry = mutableMapOf<Pair<Node, Node>, BetaNode>()

        val workGraphsSorted = workGraphs.sortedBy { workGraph ->
            with(workGraph.second) {
                edgeSet().map { getEdgeWeight(it) }.min()
            }
        }

        do {
            val complete = workGraphsSorted.map { workGraph ->
                workGraph.second.reduceWorkGraph(betaNodeRegistry)
            }.all { it }
        } while (!complete)

        workGraphsSorted.forEach { workGraph ->
            if (!workGraph.second.edgeSet().isEmpty())
                throw IllegalStateException("Still have edges: $workGraph")

            if (workGraph.second.vertexSet().size != 1)
                throw IllegalStateException("Still have more than one vertices: $workGraph")
        }

        // Bind remaining rete node with trigger

        roots = workGraphsSorted.map { workGraph ->
            val i = workGraph.first
            val node = workGraph.second.vertexSet().first()
            val binding = unifiedPatterns.first[i]

            // TODO : reverse binding
            val rbinding = SimpleBinding(binding.map { Pair(it.value, it.key) })
            // TODO : make p-node pass refs unchanged when no renaming mapping
            val group = unifiedPatterns.second[i].group
            val prio = unifiedPatterns.second[i].priority
            val proxyNode = createProxyNode(node, rbinding)
            proxyNode.addConsumer(object : Consumer {
                override fun consume(source:
                                     Node, mdf: Modification<Binding>) {
                    val ruleClause = unifiedPatterns.second[i]
                    when (mdf) {
                        // Rule clause activated -- place it to agendaManager
                        is Assert -> {
                            if(ruleClause.trigger.checkGuard(mutableMapOf(), mdf)) {
                                log.agEvent { "+A [$group:$prio] ($mdf, $ruleClause)" }
                                agendaManager.add(mdf, ruleClause)
                            }
                            else {
                                log.agEvent { "[GuardedOut] +A [$group:$prio] ($mdf, $ruleClause)\"" }
                            }
                        }
                        // Rule clause deactivated -- remove it from agendaManager
                        is Retire -> {
                            val mdfInv = mdf.inverse()
                            if (!agendaManager.remove(mdfInv, ruleClause)) {
                                if(ruleClause.trigger.filter() == AssertOnly){
                                    log.agEvent { "[FilteredOut] +A [$group:$prio] ($mdf, $ruleClause)\"" }
                                } else {
                                    if(ruleClause.trigger.checkGuard(mutableMapOf(), mdf)) {
                                        agendaManager.add(mdf, ruleClause)
                                        log.agEvent { "+A [$group:$prio] ($mdf, $ruleClause)\"" }
                                    }
                                    else {
                                        log.agEvent { "[GuardedOut] +A [$group:$prio] ($mdf, $ruleClause)\"" }
                                    }
                                }
                            } else
                                log.agEvent { "-A [$group:$prio] ($mdfInv, $ruleClause)\"" }
                        }
                    }
                }
            })
            Pair(unifiedPatterns.second[i], proxyNode)
        }

        val nodes0 = mutableSetOf<Node>().apply {
            workGraphs.forEach { wg -> addAll(wg.second.vertexSet()) }
        }
        allNodes = nodes0.subtract(Optimizer(this).optimize(nodes0))

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
            EdgeFactory { l, r ->
                val s = l.refs.intersect(r.refs)
                if (s.isEmpty()) null
                else Pair(l, r)
            })

    internal abstract fun createBetaNode(f1: Node, f2: Node): BetaNode
    internal abstract fun createAlphaNode(f1: Fact): AlphaNode
    internal abstract fun createProxyNode(node: Node, renamingData: Binding): ProxyNode
}