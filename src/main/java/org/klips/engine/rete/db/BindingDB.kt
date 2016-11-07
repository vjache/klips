package org.klips.engine.rete.db

import org.klips.dsl.Facet
import org.klips.engine.Binding

class BindingDB(val dbId: Int, val base:Binding) : Binding(){
    override val entries: Set<Map.Entry<Facet<*>, Facet<*>>>
        get() = base.entries
    override val keys: Set<Facet<*>>
        get() = base.keys
    override val size: Int
        get() = base.size
    override val values: Collection<Facet<*>>
        get() = base.values

    override fun containsKey(key: Facet<*>) = base.containsKey(key)
    override fun containsValue(value: Facet<*>) = base.containsValue(value)
    override fun get(key: Facet<*>) = base[key]
    override fun isEmpty() = base.isEmpty()
}