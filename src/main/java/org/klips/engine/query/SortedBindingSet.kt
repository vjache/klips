package org.klips.engine.query

import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding

class SortedBindingSet (baseSet:BindingSet,
                        sortRefs:List<FacetRef<*>>): BindingSet {

    private val sortedList: List<Binding> by lazy{
        baseSet.sortedWith(BindingComparator(sortRefs))
    }

    override val refs: Set<FacetRef<*>> = baseSet.refs
    override val size: Int
    get(){
        return sortedList.size
    }

    override fun isEmpty()  = size == 0
    override fun iterator() = sortedList.iterator()

    override fun toString() =
            joinToString(prefix = "${javaClass.simpleName}", postfix = ")")

}

