package org.klips.engine.rete.db

interface TupleFactoryDB {
    fun createTuple(typeCode: Int, args: Array<Any>): Any
    fun tupleData(tuple: Any): Pair<Int, Array<out Any>>
}