package org.klips.engine.rete

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.util.Log
import org.klips.engine.util.SimpleEntry
import org.klips.engine.util.activationHappen
import kotlin.collections.Map.Entry


class ProxyNode(log:Log, node: Node, val renamingBinding: Binding) : Node(log), Consumer {

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
        log.reteEvent {
            activationHappen()
            "P-CONSUME: $mdf, $this"
        }
        notifyConsumers(mdf.inherit(ProxyBinding(mdf.arg)))
    }

    override fun toString() = "P-Node($refs) [${System.identityHashCode(this)}]"

    inner class ProxyBinding(val data: Binding) : Binding() {
        override val entries: Set<Entry<Facet<*>, Facet<*>>> by lazy {
            data.map { SimpleEntry(renamingBinding[it.key] ?: it.key, it.value) }.toSet()
        }

        override val keys: Set<FacetRef<*>>
            get() = this@ProxyNode.refs
        override val size: Int
            get() = keys.size
        override val values: Collection<Facet<*>>
            get () = data.values

        override fun containsKey(key: Facet<*>)     = keys.contains(key)
        override fun containsValue(value: Facet<*>) = values.contains(value)
        override fun get(key: Facet<*>)             = data[bindingRevIndex[key]]
        override fun isEmpty()                      = keys.isEmpty()

    }
}