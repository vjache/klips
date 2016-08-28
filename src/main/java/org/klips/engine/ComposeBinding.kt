package org.klips.engine

import org.klips.dsl.Facet
import kotlin.collections.Map.Entry

class ComposeBinding(val left: Binding, val right: Binding) : Binding() {
    override val entries: Set<Entry<Facet<*>, Facet<*>>> by lazy {
        left.entries.union(right.entries)
    }
    override val keys: Set<Facet<*>> by lazy {
        left.keys.union(right.keys)
    }
    override val size: Int by lazy { keys.size }
    override val values: Collection<Facet<*>> by lazy {
        val s = mutableSetOf<Facet<*>>()
        s.addAll(left.values)
        s.addAll(right.values)
        s
    }

    override fun containsKey(key: Facet<*>) = left.keys.contains(key) || right.keys.contains(key)

    override fun containsValue(value: Facet<*>) = left.values.contains(value) || right.values.contains(value)

    override fun isEmpty() = left.isEmpty() && right.isEmpty()

    override fun get(key: Facet<*>): Facet<*>? {
        val facet = left[key]
        return when(facet){
            null -> right[key]
            else -> facet
        }
    }

    constructor(p: Pair<Binding, Binding>) : this(p.first, p.second)
}