package org.klips.dsl

import org.klips.engine.Binding
import org.klips.engine.Modification

fun rules(init: RuleSet.() -> Unit): RuleSet {
    val rs = RuleSet()
    rs.init()
    return rs
}

operator fun <T:Comparable<T>> Modification<Binding>.get(f: Facet<T>) = arg.fetchValue2(f)
