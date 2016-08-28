package org.klips.engine.query

import org.klips.dsl.Facet
import org.klips.engine.Binding
import org.klips.engine.query.BindingSet
import java.util.*

class SimpleBindingSet(
        override val refs:Set<Facet.FacetRef<*>>,
        private val baseSet: Set<Binding>) :
        BindingSet {

    override val size: Int
        get() = baseSet.size

    override fun isEmpty()  = baseSet.isEmpty()
    override fun iterator() = baseSet.iterator()

}