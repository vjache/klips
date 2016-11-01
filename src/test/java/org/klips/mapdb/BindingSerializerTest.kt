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
import kotlin.test.assertEquals


class BindingSerializerTest {


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
        val set = db.treeSet("alphaBindings").serializer(BindingSerializer(refs, TestDomainTupleFactory())).createOrOpen()

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
                keySerializer(BindingSerializer(refsKey, TestDomainTupleFactory())).
                valueSerializer(BindingSerializer(refsValue, TestDomainTupleFactory())).createOrOpen()

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
                keySerializer(BindingSerializer(refsKey, TestDomainTupleFactory())).
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