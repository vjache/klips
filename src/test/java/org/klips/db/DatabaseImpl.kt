package org.klips.db

import org.klips.engine.rete.db.Database
import org.klips.engine.rete.db.Serializer
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap


class DatabaseImpl: Database {
    override fun <K, V> openMap(name: String, cmp: Comparator<K>, kser: Serializer<K>, vser: Serializer<V>): MutableMap<K, V> {
        return ConcurrentSkipListMap<K, V>(cmp)
    }

    override fun <K, V> openMultiMap(name: String, cmp: Comparator<K>, kser: Serializer<K>, vser: Serializer<V>): MutableMap<K, MutableSet<V>> {
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