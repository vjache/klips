package org.klips.dsl

internal abstract class RHSBase(validator: (Fact) -> Unit) : AsserterTrait(validator), RHS {

    override val modified = mutableListOf<Fact>()

    override fun <T : Fact> T.not(): T {
        validate(this)
        modified.add(this)
        return this
    }

}