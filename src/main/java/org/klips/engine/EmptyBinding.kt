package org.klips.engine

import org.klips.dsl.Facet

class EmptyBinding : Binding() {

    override val entries: Set<Map.Entry<Facet<*>, Facet<*>>> = emptySet()
    override val keys: Set<Facet<*>>                         = emptySet()
    override val size: Int                                   = 0
    override val values: Collection<Facet<*>>                = emptySet()

    override fun containsKey(key: Facet<*>)     = false
    override fun containsValue(value: Facet<*>) = false
    override fun get(key: Facet<*>): Facet<*>?  = null
    override fun isEmpty()                      = true

}