package org.klips.dsl

interface Asserter {

    val asserted: List<Fact>
    val retired: List<Fact>

    fun assert(vararg facts: Fact): Array<out Fact>
    fun retire(vararg facts: Fact): Array<out Fact>

    operator fun <T : Fact> T.unaryPlus() : T {
        assert(this)
        return this
    }

    operator fun <T : Fact> T.unaryMinus() : T {
        retire(this)
        return this
    }
}