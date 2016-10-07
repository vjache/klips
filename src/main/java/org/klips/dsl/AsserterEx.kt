package org.klips.dsl

interface AsserterEx : Asserter {

    val modified: List<Fact>

    operator fun Fact.not():Fact
}