package org.klips.db

import org.klips.engine.*
import org.klips.engine.rete.db.TupleFactoryDB

class TestDomainTupleFactory : TupleFactoryDB {
    override fun createTuple(typeCode: Int, args: Array<Any>): Any = when (typeCode) {
        0 -> CellId(args.first() as Int)
        1 -> ActorId(args.first() as Int)
        2 -> PlayerId(args.first() as Int)
        3 -> Level(args[0] as Float, args[1] as Float)
        4 -> ActorKind.values()[args.first() as Int]
        5 -> PlayerColor.values()[args.first() as Int]
        6 -> LandKind.values()[args.first() as Int]
        7 -> ResourceType.values()[args.first() as Int]
        8 -> State.values()[args.first() as Int]
        else -> throw IllegalArgumentException("Unexpected tuple type: $typeCode")
    }

    override fun tupleData(tuple: Any): Pair<Int, Array<out Any>> = when (tuple) {
        is CellId -> Pair(0, arrayOf(tuple.id))
        is ActorId -> Pair(1, arrayOf(tuple.id))
        is PlayerId -> Pair(2, arrayOf(tuple.id))
        is Level -> Pair(3, arrayOf(tuple.value,tuple.maxValue))
        is ActorKind -> Pair(4, arrayOf(tuple.ordinal))
        is PlayerColor -> Pair(5, arrayOf(tuple.ordinal))
        is LandKind -> Pair(6, arrayOf(tuple.ordinal))
        is ResourceType -> Pair(7, arrayOf(tuple.ordinal))
        is State -> Pair(8, arrayOf(tuple.ordinal))
        else -> throw IllegalArgumentException("Unexpected tuple type: $tuple")
    }

}