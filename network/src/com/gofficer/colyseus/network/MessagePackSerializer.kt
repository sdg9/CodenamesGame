package com.gofficer.colyseus.network

import com.daveanthonythomas.moshipack.MoshiPack
import org.msgpack.core.MessagePack
import org.msgpack.core.MessageUnpacker
import org.msgpack.value.ValueType

val moshiPack = MoshiPack()

data class ProtocolMessage(
    var protocol: Int? = null,
    var subProtocol: Int? = null,
    var message: ByteArray? = null,
    var originalMessage: ByteArray? = null
)

fun pack(protocolMessage: ProtocolMessage?): ByteArray {
    val protocol = protocolMessage?.protocol
    val subProtocol = protocolMessage?.subProtocol
    val message = protocolMessage?.message
    return moshiPack.packToByteArray(listOf(protocol, subProtocol, message))
}
fun pack(protocol: Int, subProtocol: Enum<*>, message: Any): ByteArray {
    return pack(protocol, subProtocol.ordinal, message)
}
fun pack(protocol: Int, subProtocol: Int, message: Any): ByteArray {
    return moshiPack.packToByteArray(listOf(protocol, subProtocol, message))
}


fun unpackUnknown(packed: Any): ProtocolMessage? {
    return when (packed) {
        is ByteArray -> unpackUnknown(packed)
        is ProtocolMessage -> packed // just in case i call itself on itself...
        else -> {
            println("Called unpackUnknown with unknown type as input ${packed::class.simpleName}")
            null
        }
    }
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
            return unpackLength2(unpacker, packed)
        } else if (length == 3) {
            return unpackLength3(unpacker, packed)
        } else {
            return null
        }
    } else {
        return null
    }
}

private fun unpackLength2(unpacker: MessageUnpacker, packed: ByteArray): ProtocolMessage? {
    val retVal = ProtocolMessage(originalMessage = packed)
    val firstArrayItem = unpacker.nextFormat
    if (firstArrayItem.valueType == ValueType.INTEGER) {
        retVal.protocol = unpacker.unpackInt()
    } else {
//        fail("First item must be an integer")
        return null
    }
    val remainderUnreadBytes = packed.size - unpacker.totalReadBytes.toInt()
    retVal.message = unpacker.readPayload(remainderUnreadBytes)
    return retVal
}

private fun unpackLength3(unpacker: MessageUnpacker, packed: ByteArray): ProtocolMessage? {
    val retVal = ProtocolMessage(originalMessage = packed)

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
        val remainderUnreadBytes = packed.size - unpacker.totalReadBytes.toInt()
        retVal.message = unpacker.readPayload(remainderUnreadBytes)
    }
    return retVal
}
