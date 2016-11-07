package org.klips.engine.rete.db

import org.klips.dsl.Facet
import org.klips.engine.Binding
import java.util.*

/**
 * Created by vj on 07.11.16.
 */
class BindingComparator(val refs: Collection<Facet.FacetRef<*>>) :Comparator<Binding> {
    override fun compare(first: Binding?, second: Binding?): Int {
        if (first === second) return 0

        if (first == null) return 1

        if (second == null) return -1

        for(k in refs)
        {

            val vf = first[k]
            val vs = second[k]

            if (vf == null) {
                if (vs == null) continue
                else return 1
            } else if (vs == null)
                return -1

            val cmp = vf.compareTo(vs)
            if (cmp == 0)
                continue
            else
                return cmp

        }

        return 0
    }
}