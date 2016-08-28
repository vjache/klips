package org.klips.engine.rete.builder.optimizer

data class Signature(val nonLeafCount: Int, val deep: Int) : Comparable<Signature> {
    override fun compareTo(other: Signature): Int {
        val cmp = nonLeafCount - other.nonLeafCount
        if (cmp == 0)
            return deep - other.deep
        else
            return cmp
    }
}