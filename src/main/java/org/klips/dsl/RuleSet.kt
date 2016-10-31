package org.klips.dsl

import org.klips.engine.rete.ReteInput
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.mem.StrategyOneMem
import org.klips.engine.util.Log

/**
 * This abstract class is used to create a alphaBindings of rules.
 * Create your own class as a subclass of this one and
 * define your rules e.g. in 'init{}' like this:
 * ```
 * class MyRules : RuleSet(Log()) {
 *      init {
 *          rule {
 *          ...
 *              effect { ... }
 *          }
 *          rule {
 *          ...
 *              effect { ... }
 *          }
 *          ...
 *      }
 * }
 * ```
 */
abstract class RuleSet(val log: Log) : FacetBuilder() {

    private val rules: MutableList<Rule> = mutableListOf()

    var rete: ReteBuilderStrategy? = null
    get() {
        if (field == null)
        {
            field = StrategyOneMem(log, rules.map(Rule::toInternal))
        }
        return field
    }

    private var defaultPrioClock = 0.0

    /**
     * This property is used to apply modifications to the
     * working memory of this rule alphaBindings.
     * @see org.klips.engine.rete.ReteInput
     */
    val input: ReteInput
        get() = rete!!.input

    /**
     * Use this high order function to define a rule. It is expected
     * that lambda passed to this function constructs a pattern and
     * returns an RHSImpl i.e. effect. The pattern constructed using unary
     * operators + and - which accept a fact pattern.
     *
     * Rule can be named, use 'name' parameter to pass name. Name of
     * a rule is used when we want to ensure that rule is triggered
     * when use high order function [org.klips.engine.rete.ReteInput.flush].
     *
     * Rule can be prioritized, use 'priority' to pass priority. The priorities
     * are used to decide which activated rule apply first if there are more
     * than one active rules in agenda
     *
     * @see Rule
     * @see Rule.effect
     * @see Fact
     */
    fun rule(name: String = "*", priority: Double? = null, init: Rule.() -> RHS) {
        val lhs = Rule(name, priority ?: defaultPrioClock)
        defaultPrioClock += 100.0
        lhs.init()
        rules.add(lhs)
        rete = null
    }

    fun printSummary() {
        rete!!.printSummary()
    }
}