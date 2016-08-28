package org.klips.dsl

open class AsserterTrait : Asserter {
    override val asserted = mutableListOf<Fact>()
    override val retired = mutableListOf<Fact>()

    override fun assert(vararg facts: Fact): Array<out Fact> {
        asserted.addAll(facts)
        return facts
    }

    override fun retire(vararg facts: Fact): Array<out Fact> {
        retired.addAll(facts)
        return facts
    }
}