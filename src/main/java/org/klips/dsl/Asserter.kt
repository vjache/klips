package org.klips.dsl

interface Asserter {

    val asserted: List<Fact>
    val retired: List<Fact>

    fun assert(vararg facts: Fact): Array<out Fact>
    fun retire(vararg facts: Fact): Array<out Fact>

    operator fun Fact.unaryPlus():Fact {
        assert(this)
        return this
    }

    operator fun Fact.unaryMinus():Fact {
        retire(this)
        return this
    }
}