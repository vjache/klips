package org.klips.engine

import org.klips.dsl.Facet
import org.klips.engine.util.SimpleEntry

class SingletonBinding(val entry: Map.Entry<Facet<*>, Facet<*>>)  : Binding(){

    override val entries : Set<Map.Entry<Facet<*>, Facet<*>>> = setOf(entry)
    override val keys    : Set<Facet<*>>                  = setOf(entry.key)
    override val size    : Int                            = 1
    override val values  : Collection<Facet<*>>           = setOf(entry.value)

    override fun containsKey(key: Facet<*>)     = key == entry.key
    override fun containsValue(value: Facet<*>) = value == entry.value
    override fun get(key: Facet<*>)             = if (key == entry.key) entry.value else null
    override fun isEmpty()                      = false
}