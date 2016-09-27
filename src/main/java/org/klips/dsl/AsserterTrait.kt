package org.klips.dsl

open class AsserterTrait : Asserter {
    override val asserted = mutableListOf<Fact>()
    override val retired  = mutableListOf<Fact>()

    val factValidator : ((Fact) -> Unit)?

    override fun assert(vararg facts: Fact): Array<out Fact> {
        validate(*facts)
        asserted.addAll(facts)
        return facts
    }

    override fun retire(vararg facts: Fact): Array<out Fact> {
        validate(*facts)
        retired.addAll(facts)
        return facts
    }

    constructor(validator: (Fact) -> Unit) { factValidator = validator}

    private fun validate(vararg facts: Fact) {
        factValidator?.let {
            for (f in facts) it(f)
        }
    }
}