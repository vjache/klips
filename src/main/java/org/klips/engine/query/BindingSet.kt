package org.klips.engine.query

import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding


interface BindingSet : Iterable<Binding> {
    val refs : Set<FacetRef<*>>

    val size: Int
    fun isEmpty(): Boolean
}