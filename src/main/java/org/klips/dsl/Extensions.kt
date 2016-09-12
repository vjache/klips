package org.klips.dsl

import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding
import org.klips.engine.Modification
import java.util.concurrent.atomic.AtomicInteger

fun rules(init: RuleSet.() -> Unit): RuleSet {
    val rs = RuleSet()
    rs.init()
    return rs
}

operator fun <T:Comparable<T>> Modification<Binding>.get(f: Facet<T>) = arg.fetchValue2(f)

val <T:Comparable<T>> T.facet:Facet<T>
    get() = Facet.ConstFacet(this)

private val refCnt = AtomicInteger(0)

fun <T:Comparable<T>> ref(name:String? = null) =
        if (name == null)
            FacetRef<T>("_${refCnt.andIncrement}")
        else
            FacetRef<T>(name)

fun Iterable<Fact>.substitute(data:Binding) = this.map { it.substitute(data) }