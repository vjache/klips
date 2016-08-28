package org.klips.engine.query

import org.klips.dsl.Facet
import org.klips.engine.Binding
import java.util.*

class BindingComparator(private val refs:List<Facet<*>>) : Comparator<Binding> {
    override fun compare(o1: Binding?, o2: Binding?): Int {
        o1?.let {
            val l1 = refs.map { o1[it] }
            o2?.let {
                val l2 = refs.map { o2[it] }
                val sz = Math.min(l1.size, l2.size)
                for(i in 0..sz-1)
                {
                    val v1 = l1[i]
                    val v2 = l2[i]

                    val cmp = compare_(v1, v2)
                    if(cmp != 0) return cmp
                }
                return l1.size - l2.size
            }
            return 1
        }
        return if(o2 == null) 0 else -1
    }

    private fun compare_(v1: Facet<*>?, v2: Facet<*>?): Int {
        // Warning: This case have sense only for full outer join
        if (v1 == null || v2 == null) return 0

        return v1.compareTo(v2)
    }
}