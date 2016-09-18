package org.klips.engine.rete

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.util.activationHappen
import kotlin.collections.Map.Entry


class ProxyNode(node: Node, val renamingBinding: Binding) : Node(), Consumer {

    var node: Node = node
    set(value) {
        value.addConsumer(this)
        field = value
    }

    init {
        node.addConsumer(this)
    }

    val bindingRevIndex = mutableMapOf<Facet<*>, Facet<*>>().apply {
        renamingBinding.forEach { put(it.value, it.key) }
    }

    @Suppress("UNCHECKED_CAST")
    override val refs: Set<FacetRef<*>> by lazy {
        val notRenamed = node.refs.minus(renamingBinding.refs as Set<FacetRef<*>>)
        (bindingRevIndex.keys as Set<FacetRef<*>>).union(notRenamed)
    }

    override fun consume(source: Node, mdf: Modification<Binding>) {
        activationHappen()
        notifyConsumers(mdf.inherit(ProxyBinding(mdf.arg)))
    }

    override fun toString() = "P-Node($refs) [${System.identityHashCode(this)}]"

    inner class ProxyBinding(val data: Binding) : Binding() {
        override val entries: Set<Entry<Facet<*>, Facet<*>>> by lazy {
            data.map { object : Entry<Facet<*>, Facet<*>> {
                override val key   = renamingBinding[it.key] ?: it.key
                override val value = it.value
            } }.toSet()
        }

        override val keys   = this@ProxyNode.refs
        override val size   = keys.size
        override val values = data.values

        override fun containsKey(key: Facet<*>)     = keys.contains(key)
        override fun containsValue(value: Facet<*>) = values.contains(value)
        override fun get(key: Facet<*>)             = data[bindingRevIndex[key]]
        override fun isEmpty()                      = keys.isEmpty()

    }
}