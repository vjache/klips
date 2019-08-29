package org.klips.dsl

import org.klips.PatternNotConnectedException
import org.klips.dsl.ActivationFilter.AssertOnly
import org.klips.dsl.Guard.Junction
import org.klips.dsl.Guard.Junction.And
import org.klips.dsl.Guard.MultiJunction
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.Trigger
import kotlin.properties.Delegates.notNull

/**
 * This class defines a production rule. Such a rule have
 * its condition (LHS), additional constraint (guard) and effect (RHS).
 *
 * Condition is a pattern described by a alphaBindings of facts with references
 * and constants. The pattern must be connected by references.
 *
 * The rule condition is evaluated against working memory of
 * a rule alphaBindings to check whether it is activated. If rule become
 * active it is scheduled to trigger i.e. it is enqueued to the agendaManager.
 *
 * The agendaManager is a priority queue, where priority is a double value and
 * the lesser value the higher priority.
 *
 * This class is not intended to be used directly but using high
 * order function [RuleSet.rule]. Example:
 * ```
 * rule (priority = 0.5) {
 *  +SomeFact(x,y)
 *  +SomeOtherFact(x,z,w)
 *  guard( x gt z )
 *  effect {
 *      +SomeThirdFact(x,y,w)
 *  }
 * }
 * ```
 * @see guard
 * @see effect
 * @see RuleSet.rule
 * @see Fact
 */
class Rule(val group: String, val priority: Double) : FacetBuilder(), Asserter by AsserterTrait({}) {

    private val guards = MultiJunction(And)
    private var rhs by notNull<RHSImpl>()

    val refs: Set<Facet.FacetRef<*>> by lazy {
        mutableSetOf<Facet.FacetRef<*>>().apply {
            asserted.forEach { fact -> addAll(fact.refs) }
            retired.forEach { fact -> addAll(fact.refs) }
        }
    }

    /**
     * This is a high order function to define an effect of a rule.
     * Effect of a rule contains an assertions or retirements of a
     * new entailed facts based on rule condition. Also effect may
     * contain a side effect e.g. send email. Actually effect may
     * be arbitrary Kotlin code if one wish.
     */
    fun effect(activation: ActivationFilter = AssertOnly,
               init: RHS.(Modification<Binding>) -> Unit): RHS {
        rhs = RHSImpl(this, activation, init)
        checkConnectedByRef(asserted.union(retired))
        return rhs
    }

    private fun checkConnectedByRef(pattern: Set<Fact>) {
        val first  = pattern.first()
        val refs   = first.refs
        val others = pattern.minus(first)
        val reminder = checkConnectedByRef(refs.toSet(), others)
        if (reminder.size > 0)
            throw PatternNotConnectedException(pattern.minus(reminder), reminder)
    }
    private fun checkConnectedByRef(refs: Set<Facet<*>>, pattern: Set<Fact>) : Set<Fact> {
        pattern.find { fact -> fact.refs.intersect(refs).size > 0 }?.let {
            return checkConnectedByRef(refs.union(it.refs), pattern.minus(it))
        }

        return pattern
    }

    /**
     * Adds a guard constraint t the rule.
     * Example:
     * ```
     * guard( (x gt y) and (x lt z) ) // x > y && x < z
     * ```
     */
    fun guard(vararg g: Guard) = guard(And, *g)

    fun guard(j: Junction, vararg g: Guard) =
            guards.juncts.add(MultiJunction(j, *g))

    /**
     * This high order function used to construct a guard directly against solution.
     * Example:
     * ```
     * guard { sol ->
     *  sol[x] > sol[y] && sol[x] < sol[z]
     * } // x > y && x < z
     * ```
     */
    fun guard(l: (Modification<Binding>) -> Boolean) {
        guards.juncts.add(Guard.LambdaGuard(l))
    }

    /**
     * This is a special execution guard. Some times it is required to
     * suppress recursive execution of a rule during one 'flush' operation.
     * Example:
     * ```
     * rule {
     *  +SomeFact(id, x)
     *  guard (x gt 5.facet)
     *  effect { sol ->
     *      -SomeFact(id, x)
     *      +SomeFact(sol[id].facet, (sol[x] - 5).facet)
     *  }
     * }
     * ...
     * input.flush { +SomeFact(100.facet) }
     * ```
     * In the example above rule will be triggered 19 times until 'x' become equal to 5.
     * So, for some cases it is good expected behaviour, but for some other we may want
     * to restrict this rule t trigger once per 'flush', this depends on particular domain.
     *
     * Example with recursion suppressed:
     * ```
     * rule {
     *  +SomeFact(id, x)
     *  guard (x gt 5.facet)
     *  onceBy(id)
     *  effect { sol ->
     *      -SomeFact(id, x)
     *      +SomeFact(sol[id].facet, (sol[x] - 5).facet)
     *  }
     * }
     * ...
     * input.flush { +SomeFact(100.facet) }
     * ```
     * In the example above please note that we pass facet 'id' to the guard 'onceBy',
     * this mean that the rule would be triggered once per combination of facets
     * (in our example the combination consists of one facet).
     */
    fun onceBy(vararg facets:Facet<*>) {
        guard(Guard.OnceBy(group, *facets))
    }

    internal fun toInternal(): RuleClause {
        val pattern = mutableSetOf<Fact>().apply {
            addAll(retired)
            addAll(asserted)
        }
        return RuleClause(pattern, object : Trigger {
            override fun checkGuard(cache: MutableMap<Any,Any>,
                                    solution: Modification<Binding>) = guards.eval(cache, solution)

            override fun filter(): ActivationFilter {
                return rhs.filter?:let { AssertOnly }
            }

            override fun fire(cache: MutableMap<Any,Any>,
                              solution: Modification<Binding>,
                              addEffect: (Modification<Fact>) -> Unit) {
                if (checkGuard(cache, solution)) {
                    rhs.init(solution)

                    // 1. Retire phase
                    retired.forEach { addEffect(Retire(it.substitute(solution))) }
                    rhs.retired.forEach { addEffect(Retire(it.substitute(solution))) }
                    if (solution is Retire)
                        rhs.modified.forEach { addEffect(Retire(it.substitute(solution))) }
                    // 2. Assert phase
                    rhs.asserted.forEach { addEffect(Assert(it.substitute(solution))) }
                    if (solution is Assert)
                        rhs.modified.forEach { addEffect(Assert(it.substitute(solution))) }

                    // 3. If there is a gateway then do pass
                    rhs.gateway?.let { gw ->
                        gw.dest.flush {
                            gw.retired.forEach {
                                -it.substitute(solution)
                            }
                            gw.asserted.forEach {
                                +it.substitute(solution)
                            }
                            when(solution)
                            {
                                is Assert -> gw.modified.forEach {
                                    +it.substitute(solution)
                                }
                                is Retire -> gw.modified.forEach {
                                    -it.substitute(solution)
                                }
                            }
                        }
                    }
                }
            }
        }, group, priority)
    }
}

