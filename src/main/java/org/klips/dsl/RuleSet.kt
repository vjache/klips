package org.klips.dsl

import org.klips.engine.rete.ReteInput
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.builder.StrategyOneMem

open class RuleSet : FacetBuilder() {

    private val rules: MutableList<Rule> = mutableListOf()

    var rete: ReteBuilderStrategy? = null
    get() {
        if (field == null)
        {
            field = StrategyOneMem(rules.map { it.toInternal() })
        }
        return field
    }

    private var defaultPrioClock = 0.0

    val input: ReteInput
        get() = rete!!.input

    fun rule(name: String = "*", priority: Double? = null, init: Rule.() -> RHS) {
        val lhs = Rule(name, priority ?: defaultPrioClock)
        defaultPrioClock += 100.0
        lhs.init()
        rules.add(lhs)
        rete = null
    }
}