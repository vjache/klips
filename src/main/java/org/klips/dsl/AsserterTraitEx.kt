package org.klips.dsl

open class AsserterTraitEx(validator: (Fact) -> Unit) : AsserterTrait(validator), AsserterEx {
    override val modified = mutableListOf<Fact>()

    override fun <T : Fact> T.not(): T {
        validate(this)
        modified.add(this)
        return this
    }

}