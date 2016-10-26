package org.klips.engine

import org.klips.dsl.Facet
import org.klips.engine.util.ListSet
import org.klips.engine.util.MappedCollectionSet
import org.klips.engine.util.SimpleEntry
import kotlin.collections.Map.Entry


class ProjectBinding(override val keys: Set<Facet<*>>, val former:Binding) : Binding() {

    override val entries: Set<Entry<Facet<*>, Facet<*>>> = ListSet{
        keys.map { SimpleEntry(it, former[it]!!) } }
    override val size: Int
        get() = keys.size
    override val values: Collection<Facet<*>>
        get() = MappedCollectionSet(keys) { former[it]!! }
    override fun containsKey(key: Facet<*>)     = key in keys
    override fun containsValue(value: Facet<*>) = value in values
    override fun isEmpty()                      = keys.isEmpty()
    override fun get(key: Facet<*>)             = if (key in keys) former[key] else null
}