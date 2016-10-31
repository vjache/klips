package org.klips.engine.rete.mapdb

import org.mapdb.DataInput2
import org.mapdb.DataOutput2
import org.mapdb.Serializer

abstract class ValueSerializer() : Serializer<Any> {
    abstract fun deserializeEx(typeCodeRaw: Int, input: DataInput2, available: Int): Any
    abstract fun serializeEx(output: DataOutput2, tuple: Any)
    override fun deserialize(input: DataInput2, available: Int): Any {
        val typeCode = input.readByte().toInt()
        return when (typeCode) {
            1 -> input.readInt()
            2 -> input.readLong()
            3 -> input.readFloat()
            4 -> input.readDouble()
            5 -> input.readUTF()
            6 -> input.readBoolean()
            7 -> input.readByte()
            else -> deserializeEx(typeCode, input, available)
        }
    }

    override fun serialize(output: DataOutput2, value: Any) {
        when (value) {
            is Int -> {
                output.writeByte(1)
                output.writeInt(value)
            }
            is Long -> {
                output.writeByte(2)
                output.writeLong(value)
            }
            is Float -> {
                output.writeByte(3)
                output.writeFloat(value)
            }
            is Double -> {
                output.writeByte(4)
                output.writeDouble(value)
            }
            is String -> {
                output.writeByte(5)
                output.writeUTF(value)
            }
            is Boolean -> {
                output.writeByte(6)
                output.writeBoolean(value)
            }
            is Byte -> {
                output.writeByte(7)
                output.writeByte(value as Int)
            }
            else -> serializeEx(output, value)
        }
    }
}