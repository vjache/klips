package org.klips.mapdb

import org.junit.Test
import org.klips.dsl.Facet
import org.klips.dsl.facet
import org.klips.dsl.value
import org.klips.engine.Adjacent
import org.klips.engine.CellId
import org.mapdb.DBMaker
import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer
import org.mapdb.serializer.GroupSerializer
import org.mapdb.serializer.GroupSerializerObjectArray
import org.mapdb.serializer.SerializerArrayTuple
import org.mapdb.serializer.SerializerJava
import java.util.*


class MapDbTest {

    @Test
    fun basic() {
        val db = DBMaker.memoryDB().make()

        // initialize multimap: Map<String,List<Integer>>
        val multimap = db.treeSet("towns").serializer(SerializerArrayTuple(Serializer.STRING, Serializer.INTEGER))//alphaBindings tuple serializer
                .createOrOpen()

// populate, key is first component in tuple (array), value is second
        multimap.add(arrayOf("John", 1))
        multimap.add(arrayOf("John", 2))
        multimap.add(arrayOf("Anna", 1))

        val johnSubset = multimap.subSet(
                arrayOf<Any?>("John"), // lower interval bound
                arrayOf<Any?>("John", null))  // upper interval bound, null is positive infinity

        johnSubset.forEach { println(it) }
    }

    @Test
    fun basic1() {
        val db = DBMaker.memoryDB().make()

        // initialize multimap: Map<String,List<Integer>>
        val multimap = db.treeSet("towns").serializer(object : GroupSerializerObjectArray<Adjacent>() {
            override fun deserialize(input: DataInput2, available: Int): Adjacent {
                return Adjacent(CellId(input.readInt()).facet,CellId(input.readInt()).facet)
            }

            override fun serialize(output: DataOutput2, p1: Adjacent) {
                output.writeInt( p1.cid1.value.id )
                output.writeInt( p1.cid2.value.id )
            }

            override fun compare(first: Adjacent?, second: Adjacent?): Int {
                if (first === second) return 0

                if (first == null) return 1

                if (second == null) return -1

                if (first.cid1.compareTo(second.cid1) == 0)
                    return first.cid2.compareTo(second.cid2)

                return first.cid1.compareTo(second.cid1)
            }

        })
                .createOrOpen()

// populate, key is first component in tuple (array), value is second
        multimap.add(Adjacent(CellId(0).facet, CellId(1).facet))
        multimap.add(Adjacent(CellId(0).facet, CellId(2).facet))
        multimap.add(Adjacent(CellId(0).facet, CellId(3).facet))

        val johnSubset = multimap.tailSet(
                Adjacent(CellId(0).facet, CellId(2).facet))  // upper interval bound, null is positive infinity

        johnSubset.forEach { println(it) }
    }

}

