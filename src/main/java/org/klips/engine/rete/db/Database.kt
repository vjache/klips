package org.klips.engine.rete.db

import java.util.*


interface Database {
    fun <K, V> openMap(name:String,
                       cmp: Comparator<K>,
                       kser: Serializer<K>,
                       vser: Serializer<V>) : MutableMap<K,V>
    fun <K, V> openMultiMap(name:String,
                            cmp: Comparator<K>,
                            kser: Serializer<K>,
                            vser: Serializer<V>) : MutableMap<K, MutableSet<V>>
}

