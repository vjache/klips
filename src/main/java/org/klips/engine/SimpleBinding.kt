package org.klips.engine

import org.klips.dsl.Facet
import org.klips.engine.util.SimpleEntry
import kotlin.collections.Map.Entry


class SimpleBinding(private val data : Map<Facet<*>, Facet<*>>)  : Binding(){

    constructor(pairs:Iterable<Pair<Facet<*>, Facet<*>>>) :this(
        mutableMapOf<Facet<*>, Facet<*>>().apply{
            pairs.forEach { put(it.first, it.second) }
        })

    constructor(vararg pairs:Pair<Facet<*>, Facet<*>>):this(listOf(*pairs))

    override val entries : Set<Entry<Facet<*>, Facet<*>>> = data.entries
    override val keys    : Set<Facet<*>>                  = data.keys
    override val size    : Int                            = data.size
    override val values  : Collection<Facet<*>>           = data.values

    override fun containsKey(key: Facet<*>)     = data.containsKey(key)
    override fun containsValue(value: Facet<*>) = data.containsValue(value)
    override fun get(key: Facet<*>)             = data[key]
    override fun isEmpty()                      = data.isEmpty()
}