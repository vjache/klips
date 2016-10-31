package org.klips.dsl

import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.util.Log
import java.util.concurrent.atomic.AtomicInteger

fun rules(log: Log, init: RuleSet.() -> Unit): RuleSet {
    val rs = object : RuleSet(log) {}
    rs.init()
    return rs
}

operator fun <T:Comparable<T>> Modification<Binding>.get(f: Facet<T>) = arg.fetchValue2(f)

val <T:Comparable<T>> T.facet:Facet.ConstFacet<T>
    get() = Facet.ConstFacet(this)

val <T:Comparable<T>> Facet<T>.value:T
    get() = (this as Facet.ConstFacet<T>).value

private val refCnt = AtomicInteger(0)

fun <T:Comparable<T>> ref(name:String? = null) =
        if (name == null)
            FacetRef<T>("_${refCnt.andIncrement}")
        else
            FacetRef<T>(name)

fun <T : Fact> Iterable<T>.substitute(data:Binding)     = this.map { it.substitute(data) }
fun <T : Fact> T.substitute(data:Modification<Binding>) = this.substitute(data.arg)
fun <T : Fact> Binding.substitute(vararg facts : T) : List<T> =
        facts.map { it.substitute(this) }
fun <T : Fact> Modification<Binding>.substitute(vararg facts : T) : List<T> =
        facts.map { it.substitute(this) }

////////////////////////////////////////////////////////////////////////////////////////////////


/**
 * Creates a new fact instance form this one but with
 * some facet replaced by other using a replacement pair.
 */
fun <T : Fact> T.substitute(what:Facet<*>, with:Facet<*>) : T = this.substitute {
    when(it) {
        what -> with
        else -> null
    }
}

/**
 * Creates a new fact instance form this one but with
 * some facets replaced by others using a binding.
 */
fun <T : Fact> T.substitute(data:Binding) : T = this.substitute {
    data[it]
}

/**
 * Creates a new fact instance form this one but with
 * some facet replaced by other using a replacement pairs.
 */
fun <T : Fact> T.substitute(vararg substs : Pair<Facet<*>, Facet<*>>) : T {
    val data = mapOf(*substs)
    return this.substitute{ data[it] }
}