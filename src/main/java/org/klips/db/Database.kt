package org.klips.db

import java.util.*


interface Database {
    fun <K, V> openMap(name:String,
                       cmp:Comparator<K>,
                       kser: Serializer<K>,
                       vser: Serializer<V>) : NavigableMap<K,V>
    fun <K, V> openMultiMap(name:String,
                            cmp:Comparator<K>,
                            kser: Serializer<K>,
                            vser: Serializer<V>) : NavigableMap<K, MutableSet<V>>
}

