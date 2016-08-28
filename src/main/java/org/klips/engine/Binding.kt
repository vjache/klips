package org.klips.engine

import org.klips.dsl.Facet
import org.klips.dsl.Facet.ConstFacet

abstract class  Binding : Map<Facet<*>, Facet<*>>{
    val refs: Set<Facet<*>>
        get() = keys
    val consts: Collection<Facet<*>>
        get() = values
    private val hc by lazy {
        var c = 0
        for(v in values)
            c += v.hashCode()
        c
    }

    @Suppress("UNCHECKED_CAST")
    fun <V : Facet<*>> fetch(key: Facet<*>): V{
        return this[key] as V
    }

    fun <V : Comparable<V>> fetchValue(key: Facet<*>): V {
        return fetch<ConstFacet<V>>(key).value
    }

    fun <V : Comparable<V>> fetchValue2(key: Facet<V>): V {
        return fetch<ConstFacet<V>>(key).value
    }

    override fun hashCode() = hc

    override fun equals(other: Any?): Boolean {
        if(this === other) return true

        if(other !is Binding) return false

        if(keys.size != other.keys.size) return false

        return entries.all{other[it.key] == it.value}
    }

    override fun toString() = entries.joinToString (
            prefix = "${this.javaClass.simpleName}{",
            postfix = "}"){
        "${it.key} = ${it.value}"
    }
}

