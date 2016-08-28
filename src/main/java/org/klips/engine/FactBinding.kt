package org.klips.engine

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact

class FactBinding(val fact: Fact, val refIndex:Map<FacetRef<*>, Int>) : Binding() {
    data class EntryImpl(
            override val key: FacetRef<*>,
            override val value: Facet<*>) : Map.Entry<FacetRef<*>, Facet<*>>

    override val entries by lazy{
            refIndex.entries.map { EntryImpl(it.key, fact.facets[it.value]) }.toSet()
    }
    override val keys: Set<FacetRef<*>>
        get(){ return refIndex.keys }
    override val size: Int
        get(){ return refIndex.size }
    override val values: Collection<Facet<*>> by lazy{
        refIndex.values.map { fact.facets[it] }}
    override fun containsKey(key: Facet<*>)     = refs.contains(key)
    override fun containsValue(value: Facet<*>) = values.contains(value)
    override fun get(key: Facet<*>) = refIndex[key]?.let{fact.facets[it]}
    override fun isEmpty()  = fact.facets.isEmpty()
    override fun toString() =
            refs.joinToString (prefix = "{",postfix = "}"){ "$it = ${get(it)}" }
}