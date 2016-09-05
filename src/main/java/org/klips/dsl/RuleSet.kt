package org.klips.dsl

import org.klips.engine.rete.ReteInput
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.builder.StrategyOneMem

open class RuleSet : FacetBuilder() {

    private val rules: MutableList<Rule> = mutableListOf()

    private var rete: ReteBuilderStrategy? = null

    private var defaultPrioClock = 0.0

    val input: ReteInput
        get() {
            if (rete == null) {
                rete = StrategyOneMem(rules.map { it.toInternal() })
            }

            return rete!!.input
        }

    fun rule(name: String = "*", priority: Double? = null, init: Rule.() -> RHS) {
        val lhs = Rule(name, priority ?: defaultPrioClock)
        defaultPrioClock += 100.0
        lhs.init()
        rules.add(lhs)
        rete = null
    }
}