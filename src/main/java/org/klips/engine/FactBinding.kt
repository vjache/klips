package org.klips.engine

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.engine.util.ListSet
import org.klips.engine.util.SimpleEntry
import kotlin.collections.Map.Entry

class FactBinding(val fact: Fact, val refIndex:Map<FacetRef<*>, Int>) : Binding() {
    override val entries : Set<Entry<Facet<*>, Facet<*>>> = ListSet {
            refIndex.entries.map { SimpleEntry(it.key, fact.facets[it.value]) }
    }
    override val keys: Set<FacetRef<*>>
        get(){ return refIndex.keys }
    override val size: Int
        get(){ return refIndex.size }
    override val values: Collection<Facet<*>> = ListSet{refIndex.values.map { fact.facets[it] }}
    override fun containsKey(key: Facet<*>)     = refs.contains(key)
    override fun containsValue(value: Facet<*>) = values.contains(value)
    override fun get(key: Facet<*>) = refIndex[key]?.let{fact.facets[it]}
    override fun isEmpty()  = fact.facets.isEmpty()
    override fun toString() =
            refs.joinToString (prefix = "{",postfix = "}"){ "$it = ${get(it)}" }
}