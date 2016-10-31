package org.klips.engine.util

import org.klips.dsl.Facet

data class SimpleEntry<out K, out V>(
        override val key: K,
        override val value: V) : Map.Entry<K, V>

public infix fun <A, B> A.to(that: B): Map.Entry<A, B> = SimpleEntry(this, that)