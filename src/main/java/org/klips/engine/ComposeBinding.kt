package org.klips.engine

import org.klips.dsl.Facet
import org.klips.engine.util.MappedCollectionSet
import kotlin.collections.Map.Entry

class ComposeBinding(val left: Binding, val right: Binding) : Binding() {

    //    override val entries: Set<Entry<Facet<*>, Facet<*>>> =
//            ListSet {
//                left.keys.union(right.keys).map { k ->
//                    SimpleEntry(k, left.getOrElse(k) { right[k]!! })
//                }
//            }
    override val entries = object : Set<Entry<Facet<*>, Facet<*>>> {
        override val size: Int
            get() = left.size + right.count { it.key !in left.keys }

        override fun contains(element: Entry<Facet<*>, Facet<*>>) =
                element in left.entries || element in right.entries

        override fun containsAll(elements: Collection<Entry<Facet<*>, Facet<*>>>) =
            elements.all { it in this }

        override fun isEmpty() = this@ComposeBinding.isEmpty()

        override fun iterator(): Iterator<Entry<Facet<*>, Facet<*>>> = object : AbstractIterator<Entry<Facet<*>, Facet<*>>>() {
            val leftIt  = left.iterator()
            val rightIt = right.iterator()
            override fun computeNext() {
                if (leftIt.hasNext()) {
                    setNext(leftIt.next())
                } else if (rightIt.hasNext()) {
                    setNext(rightIt.next())
                } else done()
            }

        }

    }

    override val keys: Set<Facet<*>> = MappedCollectionSet(entries) { it.key }

    override val size: Int
        get() = keys.size

    override val values: Collection<Facet<*>> = MappedCollectionSet(entries) { it.value }

    override fun containsKey(key: Facet<*>) = left.keys.contains(key) || right.keys.contains(key)

    override fun containsValue(value: Facet<*>) = left.values.contains(value) || right.values.contains(value)

    override fun isEmpty() = left.isEmpty() && right.isEmpty()

    override fun get(key: Facet<*>): Facet<*>? {
        val facet = left[key]
        return when (facet) {
            null -> right[key]
            else -> facet
        }
    }

    constructor(p: Pair<Binding, Binding>) : this(p.first, p.second)
}