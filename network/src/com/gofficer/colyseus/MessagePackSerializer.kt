package com.gofficer.colyseus.network

import com.daveanthonythomas.moshipack.MoshiPack
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker
import org.msgpack.value.ValueType

val moshiPack = MoshiPack()

data class ProtocolMessage(
    var protocol: Int? = null,
    var subProtocol: Int? = null,
    var message: ByteArray? = null
)

fun pack(protocol: Int, subProtocol: Enum<*>, message: Any): ByteArray {
    return pack(protocol, subProtocol.ordinal, message)
}
fun pack(protocol: Int, subProtocol: Int, message: Any): ByteArray {
    return moshiPack.packToByteArray(listOf(protocol, subProtocol, message))
}


/**
 * Assumes messages are all arrays of 2 - 3 items
 * [protocol, message]
 * or
 * [protocol, subprotocol, message]
 */
fun unpackUnknown(packed: ByteArray): ProtocolMessage? {

    val unpacker = MessagePack.newDefaultUnpacker(packed)
    val format = unpacker.nextFormat
    if (format.valueType == ValueType.ARRAY) {
        val length = unpacker.unpackArrayHeader()
        if (length == 2) {
            return unpackLength2(unpacker, packed.size)
        } else if (length == 3) {
            return unpackLength3(unpacker, packed.size)
        } else {
            return null
        }
    } else {
        return null
    }
}

private fun unpackLength2(unpacker: MessageUnpacker, totalSize: Int): ProtocolMessage? {
    val retVal = ProtocolMessage()
    val firstArrayItem = unpacker.nextFormat
    if (firstArrayItem.valueType == ValueType.INTEGER) {
        retVal.protocol = unpacker.unpackInt()
    } else {
//        fail("First item must be an integer")
        return null
    }
    val remainderUnreadBytes = totalSize - unpacker.totalReadBytes.toInt()
    retVal.message = unpacker.readPayload(remainderUnreadBytes)
    return retVal
}

private fun unpackLength3(unpacker: MessageUnpacker, totalSize: Int): ProtocolMessage? {
    val retVal = ProtocolMessage()

    val firstArrayItem = unpacker.nextFormat
    if (firstArrayItem.valueType == ValueType.INTEGER) {
        retVal.protocol = unpacker.unpackInt()
    } else {
//        fail("First item must be an integer")
        return null
    }
    val secondArrayItem = unpacker.nextFormat
    if (secondArrayItem.valueType == ValueType.INTEGER) {
        retVal.subProtocol = unpacker.unpackInt()
    }
    val thirdArrayItem = unpacker.nextFormat
    if (thirdArrayItem.valueType == ValueType.MAP) {

//        println("Total read: ${unpacker.totalReadBytes}")

        // Only works when this is the last item, should be sufficient for what I need
        val remainderUnreadBytes = totalSize - unpacker.totalReadBytes.toInt()
        retVal.message = unpacker.readPayload(remainderUnreadBytes)
    }
    return retVal
}