package org.klips.dsl

import org.klips.RHSFactNotBoundException
import org.klips.dsl.ActivationFilter.*
import org.klips.engine.Binding
import org.klips.engine.Modification


class RHS(private val rule : Rule,
          private val filter : ActivationFilter?,
          private val initBlock: RHS.(Modification<Binding>) -> Unit) :
        AsserterTraitEx({fact ->
            val unboundRef = fact.refs.filter { it !in rule.refs }
            if(unboundRef.size > 0) throw RHSFactNotBoundException(fact, unboundRef, rule)
        }) {

    fun init(solution: Modification<Binding>) {
        asserted.clear()
        retired.clear()
        modified.clear()
        when (filter) {
            Both -> initBlock(solution)
            AssertOnly -> if (solution is Modification.Assert) initBlock(solution)
            RetireOnly -> if (solution is Modification.Retire) initBlock(solution)
        }
    }

}