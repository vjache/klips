package org.klips.engine.rule

import org.klips.dsl.Facet
import org.klips.dsl.Fact
import org.klips.dsl.substitute
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.mem.StrategyOneMem
import org.klips.engine.rete.builder.Trigger

open class RuleTestCommon {
    fun <T : Comparable<T>> ref(id: String) = Facet.FacetRef<T>(id)

    fun createRule(vararg pattern: Fact,
                   trigger: (Modification<Binding>, (Modification<Fact>) -> Unit) -> Unit) =
            RuleClause(
                    setOf(*pattern),
                    object : Trigger {
                        override fun fire(cache: MutableMap<Any, Any>, solution: Modification<Binding>, addEffect: (Modification<Fact>) -> Unit) {
                            trigger(solution, addEffect)
                        }
                    }
            )

    fun testTriggered(vararg pattern: Fact, mdfs : () -> Array<Modification<out Fact>>){
        var triggeredCount = 0
        val rule = createRule(*pattern) { sol, effect ->
            println("Solution : $sol")
            val conditions = pattern.map {
                sol.inherit(it.substitute(sol))
            }
            println("Condition : $conditions")

            triggeredCount ++
        }

        StrategyOneMem(rule).input.apply {
            modify(*mdfs())
        }.flush()

        assert(triggeredCount > 0)
    }

}