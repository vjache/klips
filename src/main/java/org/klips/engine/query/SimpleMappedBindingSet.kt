package org.klips.engine.query

import org.klips.dsl.Facet
import org.klips.engine.Binding
import org.klips.engine.util.MappedIterator

class SimpleMappedBindingSet(
        override val refs:Set<Facet.FacetRef<*>>,
        private val baseSet: Set<Binding>,
        private val mapper:(Binding) -> Binding) :
        BindingSet {

    override val size: Int
        get() = baseSet.size

    override fun isEmpty()  = baseSet.isEmpty()
    override fun iterator() = MappedIterator(baseSet.iterator(), mapper)

}