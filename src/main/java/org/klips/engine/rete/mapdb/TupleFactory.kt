package org.klips.engine.rete.mapdb

interface TupleFactory {
    fun createTuple(typeCode: Int, args: Array<Any>): Any
    fun tupleData(tuple: Any): Pair<Int, Array<out Any>>
}