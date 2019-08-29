package org.klips.dsl

import org.klips.RHSFactNotBoundException
import org.klips.dsl.ActivationFilter.*
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.ReteInput


internal class RHSImpl(private val rule : Rule,
              val filter : ActivationFilter?,
              private val initBlock: RHSImpl.(Modification<Binding>) -> Unit) :
        RHS,
        RHSBase({validate(rule, it)}) {

    class AsserterGateway(
            val rule : Rule,
            val dest:ReteInput,
            val block: RHS.(Modification<Binding>) -> Unit): RHSBase({validate(rule, it)}) {
        override fun ReteInput.pass(block: RHS.(Modification<Binding>) -> Unit) {
            throw UnsupportedOperationException("not implemented")
        }

        fun init(sol:Modification<Binding>)
        {
            this.asserted.clear()
            this.retired.clear()
            this.modified.clear()
            this.block(sol)
        }
    }

    var gateway : AsserterGateway? = null

    fun init(solution: Modification<Binding>) {
        asserted.clear()
        retired.clear()
        modified.clear()
        when (filter) {
            Both -> initBlock(solution)
            AssertOnly -> if (solution is Modification.Assert) initBlock(solution)
            RetireOnly -> if (solution is Modification.Retire) initBlock(solution)
        }

        gateway?.init(solution)
    }

    override fun ReteInput.pass(block: RHS.(Modification<Binding>) -> Unit) {
        gateway = AsserterGateway(rule, this, block)
    }

    companion object {
        private fun validate(rule: Rule, fact: Fact) {
            val unboundRef = fact.refs.filter { it !in rule.refs }
            if(unboundRef.size > 0) throw RHSFactNotBoundException(fact, unboundRef, rule)
        }
    }
}