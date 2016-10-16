package org.klips.dsl

import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.ReteInput


interface RHS : Asserter {

    val modified: List<Fact>

    /**
     * This method is designed to flush facts into WM of other rule set.
     * One advantage from using standard [ReteInput.flush] is that
     * refs in aa asserted or retired facts are automatically substituted
     * with current solution of this RHS. Another feature is to use unary
     * operator '!' which is also sensitive to current solution.
     *
     * If to use [ReteInput.flush] to pass facts to other rule set WM:
     * ```
     * rule {
     *  +SomeFact(x,y)
     *  effect { sol ->
     *      +SomeThirdFact(x,y,w)
     *      otherRuleSet.input.pass {
     *          +SomeAnotherFact(x,y,w).substitute(sol)
     *      }
     *  }
     * }
     * ```
     * If to use [pass] to pass facts to other rule set WM:
     * ```
     * rule {
     *  +SomeFact(x,y)
     *  effect {
     *      +SomeThirdFact(x,y,w)
     *      otherRuleSet.input.pass {
     *          +SomeAnotherFact(x,y,w)
     *      }
     *  }
     * }
     * ```
     * But if
     */
    fun ReteInput.pass(block:RHS.(Modification<Binding>) -> Unit)

    operator fun <T : Fact> T.not():T
}