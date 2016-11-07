package org.klips.engine.rete.db

import java.io.DataInput
import java.io.DataOutput

interface Serializer<T> {
    fun serialize(out: DataOutput, obj:T) : Unit
    fun deserialize(inp: DataInput) : T

    companion object {
        val INT : Serializer<Int> = object : Serializer<Int> {
            override fun serialize(out: DataOutput, obj: Int)  = out.writeInt(obj)
            override fun deserialize(inp: DataInput) = inp.readInt()
        }
    }
}