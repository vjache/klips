package org.klips.dsl

interface Asserter {

    val asserted: List<Fact>
    val retired: List<Fact>

    fun assert(vararg facts: Fact): Array<out Fact>
    fun retire(vararg facts: Fact): Array<out Fact>

    fun Fact.assert(): Array<out Fact> = assert(this)
    fun Fact.retire(): Array<out Fact> = retire(this)
}