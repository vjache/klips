package org.klips.dsl

import org.klips.dsl.Guard.Junction
import org.klips.dsl.Guard.Junction.And
import org.klips.dsl.Guard.MultiJunction
import org.klips.engine.Binding
import org.klips.dsl.ActivationFilter
import org.klips.engine.Modification
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.dsl.ActivationFilter.AssertOnly
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.rete.builder.Trigger
import kotlin.properties.Delegates.notNull

class Rule(val group: String, val priority: Double) : FacetBuilder(), Asserter by AsserterTrait()
{

  private val events = mutableListOf<Fact>()
  private val guards = MultiJunction(And)
  private var rhs by notNull<RHS>()

  fun event(vararg o: Fact) = events.addAll(o)
  fun Fact.event() = event(this)

  fun effect(activation: ActivationFilter = AssertOnly,
             init: RHS.(Modification<Binding>) -> Unit): RHS
  {
    rhs = RHS(activation, init)
    return rhs
  }

  fun guard(vararg g: Guard) = guard(And, *g)

  fun guard(j: Junction, vararg g: Guard) =
          guards.juncts.add(MultiJunction(j, *g))

  fun guard(l: (Modification<Binding>) -> Boolean)
  {
      guards.juncts.add(Guard.LambdaGuard(l))
  }

  fun toInternal(): RuleClause
  {
    val pattern = mutableSetOf<Fact>().apply {
      addAll(events)
      addAll(retired)
      addAll(asserted)
    }
    return RuleClause(pattern, object : Trigger
    {
      override fun fire(solution: Modification<Binding>,
                        addEffect: (Modification<Fact>) -> Unit)
      {
        if (guards.eval(solution))
        {
          rhs.init(solution)

          if (solution is Assert)
          {
            events.forEach { addEffect(Retire(it.substitute(solution.arg))) }
            retired.forEach { addEffect(Retire(it.substitute(solution.arg))) }
            rhs.retired.forEach { addEffect(Retire(it.substitute(solution.arg))) }
            rhs.asserted.forEach { addEffect(Assert(it.substitute(solution.arg))) }
          }
        } else if (solution is Assert)
        {
          events.forEach { addEffect(Retire(it.substitute(solution.arg))) }
        }
      }
    }, group, priority)
  }
}