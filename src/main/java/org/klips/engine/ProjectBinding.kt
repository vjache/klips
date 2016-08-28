package org.klips.engine

import org.klips.dsl.Facet


class ProjectBinding(subRefs: Set<Facet<*>>, former:Binding) : Binding() {
    override val entries: Set<Map.Entry<Facet<*>, Facet<*>>>
        get() = subMap.entries
    override val size: Int
        get() = subMap.size
    override val values: Collection<Facet<*>>
        get() = subMap.values
    override fun containsKey(key: Facet<*>)       = subMap.containsKey(key)
    override fun containsValue(value: Facet<*>) = subMap.containsValue(value)
    override fun isEmpty() = subMap.isEmpty()

    override val keys: Set<Facet<*>>
        get() = subMap.keys

    private val subMap = mutableMapOf<Facet<*>, Facet<*>>()

    init{
        for(ref in subRefs){
            val f = former[ref]
            if(f != null) subMap[ref] = f
        }
    }
    override fun get(key: Facet<*>) = subMap[key]

}