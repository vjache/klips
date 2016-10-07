package org.klips.dsl

open class AsserterTraitEx(validator: (Fact) -> Unit) : AsserterTrait(validator), AsserterEx {
    override val modified = mutableListOf<Fact>()

    override fun Fact.not(): Fact {
        validate(this)
        modified.add(this)
        return this
    }

}