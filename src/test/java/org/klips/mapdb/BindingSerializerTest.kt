package org.klips.mapdb

import org.junit.Test
import org.klips.dsl.Facet
import org.klips.dsl.facet
import org.klips.dsl.ref
import org.klips.engine.*
import org.klips.engine.rete.mapdb.BindingSerializer
import org.klips.engine.rete.mapdb.TupleFactory
import org.mapdb.BTreeMap
import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.util.*
import kotlin.test.assertEquals

/**
 * Created by vj on 28.10.16.
 */
class BindingSerializerTest {

    class MyTupleFactory : TupleFactory {
        override fun createTuple(typeCode: Int, args: Array<Any>): Any = when (typeCode) {
            0 -> CellId(args.first() as Int)
            1 -> ActorId(args.first() as Int)
            2 -> PlayerId(args.first() as Int)
            3 -> Level(args[0] as Float,args[1] as Float)
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

    val db = DBMaker.memoryDB().make()

    @Test
    fun setTest() {

        val r1 = ref<Int>("r1")
        val r2 = ref<Int>("r2")
        val r3 = ref<Int>("r3")
        val r4 = ref<Int>("r4")
        val r5 = ref<Int>("r5")

        val refs = arrayListOf(r1, r2, r3, r4, r5)
        // initialize alphaBindings: Map<String,List<Integer>>
        val set = db.treeSet("alphaBindings").serializer(BindingSerializer(refs, MyTupleFactory())).createOrOpen()

        set.add(SimpleBinding(r1 to 1.facet, r2 to 1.facet, r3 to CellId(0).facet, r4 to Level(0f,0f).facet, r5 to ActorKind.Aim.facet))
        set.add(SimpleBinding(r1 to 1.facet, r2 to 2.facet, r3 to CellId(1).facet, r4 to Level(10f,10f).facet, r5 to ActorKind.Comm.facet))
        set.add(SimpleBinding(r1 to 2.facet, r2 to 3.facet, r3 to CellId(2).facet, r4 to Level(20f,20f).facet, r5 to ActorKind.Guard.facet))
        set.add(SimpleBinding(r1 to 2.facet, r2 to 4.facet, r3 to CellId(3).facet, r4 to Level(30f,30f).facet, r5 to ActorKind.Solar.facet))
        set.add(SimpleBinding(r1 to 3.facet, r2 to 5.facet, r3 to CellId(4).facet, r4 to Level(40f,40f).facet, r5 to ActorKind.Worker.facet))

        var subset = set.tailSet(
                SimpleBinding(r1 to 1.facet, r2 to 1.facet))  // upper interval bound, null is positive infinity

        subset.forEach { println(it) }

        assertEquals(4, subset.size)


        println("----------------------")

        subset = set.subSet(
                SimpleBinding(r1 to 1.facet, r2 to Int.MIN_VALUE.facet),
                SimpleBinding(r1 to 1.facet, r2 to Int.MAX_VALUE.facet))  // upper interval bound, null is positive infinity

        subset.forEach { println(it) }
    }

    @Test
    fun mapTest() {
        val r1 = ref<Int>("r1")
        val r2 = ref<Int>("r2")
        val r3 = ref<Int>("r3")
        val r4 = ref<Int>("r4")
        val r5 = ref<Int>("r5")

        val refsKey = arrayListOf(r1, r2)
        val refsValue = arrayListOf(r3, r4, r5)
        // initialize multimap: Map<String,List<Integer>>
        val map = db.treeMap("map").
                keySerializer(BindingSerializer(refsKey, MyTupleFactory())).
                valueSerializer(BindingSerializer(refsValue, MyTupleFactory())).createOrOpen()

        map.put(SimpleBinding(r1 to 1.facet, r2 to 1.facet),
                SimpleBinding(r3 to CellId(0).facet, r4 to Level(0f,0f).facet, r5 to ActorKind.Aim.facet))

        println(map[SimpleBinding(r1 to 1.facet, r2 to 1.facet)])
    }

    @Test
    fun mapIdTest() {
        val r1 = ref<Int>("r1")
        val r2 = ref<Int>("r2")
        val r3 = ref<Int>("r3")
        val r4 = ref<Int>("r4")
        val r5 = ref<Int>("r5")

        val refsKey = arrayListOf(r1, r2, r3, r4, r5)
        // initialize multimap: Map<String,List<Integer>>
        val map: BTreeMap<Binding, Int> = db.treeMap("map").
                keySerializer(BindingSerializer(refsKey, MyTupleFactory())).
                valueSerializer(Serializer.INTEGER).createOrOpen()

        val ids = db.atomicInteger("ids").createOrOpen()
        //
        map.put(SimpleBinding(r1 to 1.facet, r2 to 1.facet, r3 to CellId(0).facet, r4 to Level(0f,0f).facet, r5 to ActorKind.Aim.facet),
                ids.andIncrement )
        var prevId = map.putIfAbsent(
                SimpleBinding(
                        r1 to 1.facet, r2 to 1.facet, r3 to CellId(0).facet, r4 to Level(0f,0f).facet, r5 to ActorKind.Aim.facet),
                        ids.andIncrement )
        println(prevId)
        //
        prevId = map.putIfAbsent(
                SimpleBinding(
                        r1 to 1.facet, r2 to 1.facet, r3 to CellId(0).facet, r4 to Level(0f,0f).facet, r5 to ActorKind.Aim.facet),
                        ids.andIncrement )
        println(prevId)
        //
        prevId = map.putIfAbsent(
                SimpleBinding(
                        r1 to 1.facet, r2 to 2.facet, r3 to CellId(0).facet, r4 to Level(0f,0f).facet, r5 to ActorKind.Aim.facet),
                        ids.andIncrement )
        println(prevId)
        //
        for(e in map.entries)
            println(e)

        val rmId = map.remove(SimpleBinding(
                r1 to 1.facet, r2 to 2.facet, r3 to CellId(0).facet, r4 to Level(0f,0f).facet, r5 to ActorKind.Aim.facet))
        println("removed: $rmId")
        //
        for(e in map.entries)
            println(e)
    }
}