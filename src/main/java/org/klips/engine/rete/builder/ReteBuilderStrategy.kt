package org.klips.engine.rete.builder

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.query.JoinBindingSet.JoinType.FullOuter
import org.klips.engine.rete.AlphaNode
import org.klips.engine.rete.Node
import org.klips.engine.rete.ReteInput
import org.klips.engine.util.printTree

abstract class ReteBuilderStrategy(patterns : List<RuleClause>) {

    protected val unifiedPatterns = findBestSubstitutions(patterns)

    abstract val input      : ReteInput
    abstract val alphaLayer : Set<AlphaNode>
    abstract val allNodes   : Set<Node>
    abstract val roots      : List<Pair<RuleClause, Node>>

    private fun findBestSubstitutions(patts: List<RuleClause>):
            Pair<List<Map<Facet<*>, Facet<*>>>,
                 List<RuleClause>> {
        val graph    = mutableSetOf<Fact>()
        val pattsS   = mutableListOf<RuleClause>()
        val bindings = patts.mapIndexed {i, ruleClause ->
            val (pattern, decMap) = decoratePattern(i, ruleClause.pattern)
            val bs = Util.select(graph, pattern, FullOuter)
            val bsbi = bs.filter { is_bijection(it) }
            bsbi.maxBy { it.refs.size }?.let { maxBinding ->
                // Do substitution for pattern
                val patternS = pattern.map { fact ->
                    fact.substitute(maxBinding)
                }
                // Add substituted pattern to result
                pattsS.add(ruleClause.replacePattern(patternS.toSet()))
                //
                graph.addAll(patternS)
                // Return a total renaming map to be able to recall original ref names
                decMap.mapValues { maxBinding[it.value] ?: it.value }
            } ?: let{
                pattsS.add(ruleClause.replacePattern(pattern))
                graph.addAll(pattern)
                decMap //EmptyBinding()
            }
        }

        return Pair(bindings, pattsS)
    }

    private fun is_bijection(b: Binding): Boolean {
        val m = mutableMapOf<Facet<*>, Facet<*>>()
        for ((k,v) in b.entries)
        {
            if (k is Facet.ConstFacet && k != v)
                return false

            if (v is Facet.ConstFacet && k != v)
                return false

            val k0 = m[v]
            if(k0 == null) {
                m[v] = k
            }
            else if (k0 != k) return false
        }
        return true
    }

    private fun decoratePattern(i: Int, pattern0: Set<Fact>): Pair<Set<Fact>, Map<Facet<*>, Facet<*>>> {
        val decMap = mutableMapOf<Facet<*>, Facet<*>>()
        return Pair(pattern0.map { fact ->
            fact.substitute {
                if (it is FacetRef) {
                    val decFacet = it.decorated(postfix = "_$i")
                    decMap[it] = decFacet
                    decFacet
                }
                else null
            }
        }.toSet(), decMap)
    }

    fun printSummary() {
        roots.forEach{ root ->
            val group = root.first.group
            println("Rete for '$group':")
            root.second.printTree()
        }
    }
}