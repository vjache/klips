package org.klips.db

import java.util.*
import java.util.concurrent.ConcurrentSkipListMap


class DatabaseImpl: Database {
    override fun <K, V> openMap(name: String, cmp: Comparator<K>, kser: Serializer<K>, vser: Serializer<V>): NavigableMap<K, V> {
        return ConcurrentSkipListMap<K, V>(cmp)
    }

    override fun <K, V> openMultiMap(name: String, cmp: Comparator<K>, kser: Serializer<K>, vser: Serializer<V>): NavigableMap<K, MutableSet<V>> {
        return object : ConcurrentSkipListMap<K, MutableSet<V>>(cmp) {
            override fun get(key: K): MutableSet<V>? {
                var set =  super.get(key)

                if(set == null)
                {
                    set = HashSet<V>()
                    put(key, set)
                }

                return set
            }
        }
    }
}