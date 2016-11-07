package org.klips.engine.rete.db

import org.klips.engine.rete.db.Serializer
import org.klips.dsl.Facet
import org.klips.dsl.Facet.ConstFacet
import org.klips.dsl.facet
import org.klips.engine.Binding
import org.klips.engine.SimpleBinding
import java.io.DataInput
import java.io.DataOutput
import java.util.*

@Suppress("UNCHECKED_CAST")
class BindingSerializerDB(val refs: Collection<Facet.FacetRef<*>>,
                          val tFactory: TupleFactoryDB?) : Serializer<Binding> {
    constructor(refs: Collection<Facet.FacetRef<*>>)
    : this(refs, null)

    val basic = object : ValueSerializerDB(){
        override fun deserializeEx(typeCodeRaw: Int, input: DataInput): Any {
            val typeCode = typeCodeRaw - 64
            val size = input.readShort().toInt()
            val args = Array(size){ i ->
                deserialize(input)
            }

            if (tFactory!=null)
                return tFactory.createTuple(typeCode, args)
            else
                throw IllegalArgumentException("Unexpected tuple : code = $typeCode, fields = ${Arrays.toString(args)}")
        }

        override fun serializeEx(output: DataOutput, tuple: Any) {
            val (typeCode, fields) = if (tFactory!=null)
                tFactory.tupleData(tuple)
            else
                throw IllegalArgumentException("Unexpected tuple : $tuple")
            output.writeByte(typeCode + 64)
            output.writeShort(fields.size)
            fields.forEach {this.serialize(output, it)}
        }

    }
    override fun deserialize(input: DataInput): Binding {
        val m = HashMap<Facet<*>, Facet<*>>(8)
        refs.forEach { m[it] = (basic.deserialize(input) as Comparable<Any>).facet }
        return SimpleBinding(m)
    }

    override fun serialize(output: DataOutput, p1: Binding) {
        refs.forEach {
            val constFacet = (p1[it] as ConstFacet).value!!
            basic.serialize(output, constFacet)
        }
    }

    class FacetZero<T> : Facet<T>() {
        override fun match(f: Facet<*>) = when(f) {
                is FacetZero<*> -> true
                is FacetRef<*> -> false
                else           -> false
            }

        override fun compareTo(other: Facet<*>) = when(other) {
                is FacetZero -> 0
                else -> -1
            }
    }
//
//    override fun compare(first: Binding?, second: Binding?): Int {
//        if (first === second) return 0
//
//        if (first == null) return 1
//
//        if (second == null) return -1
//
//        for(k in refs)
//        {
//
//            val vf = first[k]
//            val vs = second[k]
//
//            if (vf == null) {
//                if (vs == null) continue
//                else return 1
//            } else if (vs == null)
//                return -1
//
//            val cmp = vf.compareTo(vs)
//            if (cmp == 0)
//                continue
//            else
//                return cmp
//
//        }
//
//        return 0
//    }
}