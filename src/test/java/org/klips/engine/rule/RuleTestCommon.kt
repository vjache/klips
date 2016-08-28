package org.klips.engine.rule

import org.klips.dsl.Facet
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.StrategyOneMem
import org.klips.engine.rete.builder.Trigger

open class RuleTestCommon {
    fun <T : Comparable<T>> ref(id: String) = Facet.FacetRef<T>(id)

    fun createRule(vararg pattern: Fact,
                   trigger: (Modification<Binding>, (Modification<Fact>) -> Unit) -> Unit) =
            RuleClause(
                    setOf(*pattern),
                    object : Trigger {
                        override fun fire(solution: Modification<Binding>, addEffect: (Modification<Fact>) -> Unit) {
                            trigger(solution, addEffect)
                        }

                    }
            )

    fun testTriggered(vararg pattern: Fact, mdfs : () -> Array<Modification<out Fact>>){
        val rule = createRule(*pattern) { sol, effect ->
            println("Solution : $sol")
            val conditions = pattern.map {
                sol.inherit(it.substitute(sol.arg))
            }
            println("Condition : $conditions")
        }

        StrategyOneMem(rule).input.apply {
            modify(*mdfs())
        }
    }

}