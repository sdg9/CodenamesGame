package com.example


import com.daveanthonythomas.moshipack.MoshiPack
import com.example.common.*
import okio.BufferedSource
import org.msgpack.core.MessagePack
import org.msgpack.core.MessagePacker
import org.msgpack.value.ValueType
import kotlin.test.*
import org.msgpack.value.Value
import reactor.core.publisher.toMono


/**
 * Testing out how to conveniently pack and unpack unknown types using message pack
 *
 * Notes
 * Whenever not explicitly typed, Moshi packing/unpacking will convert any number to double
 *
 * May be easier to always work with doubles in this regard
 */
class MessagePackBetweenClientAndServer {


    @Test
    fun packBasic1() {
        val protocol = 1
        val subProtocol = 2
        val anyObj = "hello"


        val moshiPack = MoshiPack()
        val packed: BufferedSource = moshiPack.pack(MessageObject(protocol, subProtocol, anyObj))

        println(packed)

        val unpacked: MessageObject = moshiPack.unpack(packed)

        println(unpacked.protocol)
        println(unpacked.subProtocol)
        println(unpacked.message)

        assertEquals(protocol, unpacked.protocol)
        assertEquals(subProtocol, unpacked.subProtocol)
        assertEquals(anyObj, unpacked.message)
    }

    @Test
    fun packBasic2() {
        val protocol = 1
        val subProtocol = 2
        val anyObj = mapOf<Any, Any>("hi" to "ho", "good" to "bye")


        val moshiPack = MoshiPack()
        val packed: BufferedSource = moshiPack.pack(MessageObject(protocol, subProtocol, anyObj))

        println(packed)

        val unpacked: MessageObject = moshiPack.unpack(packed)

        println(unpacked.protocol)
        println(unpacked.subProtocol)
        println(unpacked.message)

        assertEquals(protocol, unpacked.protocol)
        assertEquals(subProtocol, unpacked.subProtocol)
//        assertEquals(anyObj, unpacked.message)
    }

    // TODO this fails
    /*
    @Test
    fun packWhenNotKnowingComplexType() {
        val protocol = 1
        val subProtocol = 2
        val anyObj = ComplexSubObject("hi", 1, 1.0, 3L, true, listOf(1, 2, 3))


        val moshiPack = MoshiPack()
        val packed: BufferedSource = moshiPack.pack(MessageObject(protocol, subProtocol, anyObj))

        println(packed)

        val unpacked: MessageObject = moshiPack.unpack(packed)

        println(unpacked.protocol)
        println(unpacked.subProtocol)
        println(unpacked.message)

        assertEquals(protocol, unpacked.protocol)
        assertEquals(subProtocol, unpacked.subProtocol)
        assertEquals(anyObj, unpacked.message)
    }
    */
    @Test
    fun packWhenNotKnowingComplexTypeResolved() {
        val protocol = Protocol.ROOM_DATA
        val subProtocol = 2
        val anyObj = ComplexSubObject("hi", 1, 1.0, 3L, true, listOf(1, 2, 3))

        val packed: BufferedSource = moshiPack.pack(listOf(protocol, subProtocol, anyObj))

        println()
        println(packed)

        val someObject = unpackUnknown(packed)

        assertEquals(protocol, someObject.protocol)
        assertEquals(subProtocol, someObject.subProtocol)

        // TODO figure out how to convert me to desired object
        // Ideally i don't convert to and from again, can I directly return buffer instead of desearializing?
        println("Message to convert ${someObject.message}")

        var somePack = MessagePack.newDefaultBufferPacker()
        // if we had byte array
        someObject.message?.writeTo(somePack)
        somePack.close()
        println(somePack.toByteArray())
        val unpacked: MessageComplexSubObject = moshiPack.unpack(somePack.toByteArray())

//        val unpacker = MessagePack.newDefaultUnpacker(packed)
//        val firstVal = unpacker.unpackValue()
//        println("FV: ${firstVal.valueType}")
//
//
//        if (firstVal.isArrayValue) {
//            val arrayValue = firstVal.asArrayValue()
//            val protocol = getValueAsInt(arrayValue.get(0))
//            if (protocol != null && protocol == Protocol.ROOM_DATA) {
//                val subProtocol = getValueAsInt(arrayValue.get(1))
//
//                if (subProtocol != null) {
//                    val thirdObject = arrayValue.get(2)
//                }
//            } else {
//                fail("Not an item with subprotocol")
//            }
//        } else {
//            fail("Type should be Array")
//        }

//        println(unpacked.protocol)
//        println(unpacked.subProtocol)
//        println(unpacked.message)
//
//        assertEquals(protocol, unpacked.protocol)
//        assertEquals(subProtocol, unpacked.subProtocol)
//        assertEquals(anyObj, unpacked.message)
    }

    // TODO add test where item doesn't conform to protocol (don't crash)

    // TODO add test where item doesn't have message, (don't crash on bad index)

    @Test
    fun packWhenKnowingComplexType() {
        val protocol = 1
        val subProtocol = 2
        val anyObj = ComplexSubObject("hi", 1, 1.0, 3L, true, listOf(1, 2, 3))


        val moshiPack = MoshiPack()
        val packed: BufferedSource = moshiPack.pack(MessageComplexSubObject(protocol, subProtocol, anyObj))

        println(packed)

        val unpacked: MessageComplexSubObject = moshiPack.unpack(packed)

        println(unpacked.protocol)
        println(unpacked.subProtocol)
        println(unpacked.message)

        assertEquals(protocol, unpacked.protocol)
        assertEquals(subProtocol, unpacked.subProtocol)
        assertEquals(anyObj, unpacked.message)
    }



//        val packed = pack(protocol, subProtocol, "test")
//        println(packed)

//        val packer = MessagePack.newDefaultBufferPacker()

//        packer.writePayload(listOf(
//            1,
//            2,
//            "hello"
//        ))

//        packer
//
//        val msgpack = MessagePack.newDefaultBufferPacker()
//        msgpack.
//        msgpack.write(mySuperInstance)
//

//        val unpacked = unpack(packed)
//        unpacked.forEach {
//            println(it)
//        }

//        println(plug.message)

//        val unpacker = MessagePack.newDefaultUnpacker(packed)
//        val firstVal = unpacker.unpackValue()
//        if (firstVal == ValueType.ARRAY) {
//            val arrayValue = firstVal.asArrayValue()
//            val protocol = arrayValue.get(0)
//            if (protocol.valueType == ValueType.INTEGER) {
//                val code = protocol.asIntegerValue().asInt()
//                println("Code is int! [$code]")
//            } else {
//
//            }
//        } else {
//            // TODO ERROR
//        }
//        messageUnpack.
//        assertSame(protocol, unpacked.get(0))

//        println(unpacked)
//        val moshiPack = MoshiPack()
//        val packed: BufferedSource = moshiPack.pack(MessagePackWebsitePlug())
//
//        println(packed.readByteString().hex())
    }

//    @Test
//    fun convertMessagePackToString() {
//        val bytes = ByteString.decodeHex("82a7636f6d70616374c3a6736368656d6100").toByteArray()
//
//        val moshiPack = MoshiPack()
//        val plug: MessagePackWebsitePlug = moshiPack.unpack(bytes)
//
//
//        println(plug)
//    }
//
//    @Test
//    fun packVariousWays() {
//
//        val bytes = MoshiPack().packToByteArray(MessagePackWebsitePlug())
//        val bytes2 = ByteString.decodeHex("82a7636f6d70616374c3a6736368656d6100").toByteArray()
//        val bytes3 = MoshiPack().packToByteArray(MessagePackWebsitePlug())
//
//
////        println("Hex Bytes: ${Hex.encodeHex( bytes2 ).toString()}")
////        assertNotEquals(bytes, bytes3)
//
//        val unpacked1: MessagePackWebsitePlug = MoshiPack().unpack(bytes)
////        println(unpacked1)
//
//        val unpacked2: MessagePackWebsitePlug = MoshiPack().unpack(bytes2)
////        val unpacked2: MessagePackWebsitePlug = MoshiPack().unpack(bytes2)
////        println(unpacked2)
//        val unpacked3: MessagePackWebsitePlug = MoshiPack().unpack(bytes3)
////        println(unpacked3)
//
//        assertEquals(unpacked1, unpacked2)
//        assertEquals(unpacked1, unpacked3)
//    }
//
//    @Test
//    fun whyDoByteArraysNotEqual() {
//        // TODO understand why this is the case where arrays don't equal
//        val someHexString = "82a7636f6d70616374c3a6736368656d6100"
//        val bytes1 = ByteString.decodeHex(someHexString).toByteArray()
//        val bytes2 = ByteString.decodeHex(someHexString).toByteArray()
//
//        // Not sure why but byte arrays are not equal or same despite
//        assertNotSame(bytes1, bytes2)
//        assertNotEquals(bytes1, bytes2)
//    }
//
//
//    @Test
//    fun byteArrayManipulation() {
//        val someHexString = "82a7636f6d70616374c3a6736368656d6100"
//        val bytes = ByteString.decodeHex(someHexString).toByteArray()
//        val unpackedObject1: MessagePackWebsitePlug = MoshiPack().unpack(bytes)
//
//        val packedByteArray = packProtocol(Protocol.USER_ID, bytes)
//
//        // send over socket
//
//        val unpacedBytes = unpackProtocol(packedByteArray)
//        assertEquals(unpacedBytes.protocol, Protocol.USER_ID)
//        val unpackedObject2: MessagePackWebsitePlug = MoshiPack().unpack(unpacedBytes.byteArray)
//        assertEquals(unpackedObject1, unpackedObject2)
//    }

//
//fun packProtocol(protocol: Int, bytes: ByteArray): ByteArray {
//    val outputStream = ByteArrayOutputStream()
//    outputStream.write(protocol)
//    outputStream.write(bytes)
//    return outputStream.toByteArray()
//}
//
//data class DecryptProtocol(val protocol: Int, val byteArray: ByteArray)
//
//fun unpackProtocol(bytes: ByteArray): DecryptProtocol {
//    val protocol = bytes[0].toInt()
//    val newByteArray = Arrays.copyOfRange(bytes, 1, bytes.size)
//    return DecryptProtocol(protocol, newByteArray)
//}

fun getValueAsInt(item: Value): Int? {
    if (item.valueType == ValueType.INTEGER) {
        val code = item.asIntegerValue().asInt()
        println("Code is int! [$code]")
        return code
    }
    return null
}

fun unpackUnknown(packed: BufferedSource): ProtocolMessage {
    val retVal = ProtocolMessage()

    val unpacker = MessagePack.newDefaultUnpacker(packed)
    val firstVal = unpacker.unpackValue()
    if (firstVal.isArrayValue) {
        val arrayValue = firstVal.asArrayValue()
        val protocol = getValueAsInt(arrayValue.getOrNilValue(0))
        if (protocol != null && protocol == Protocol.ROOM_DATA) {
            retVal.protocol = protocol
            val subProtocol = getValueAsInt(arrayValue.getOrNilValue(1))
            if (subProtocol != null) {
                retVal.subProtocol = subProtocol
                retVal.message = arrayValue.getOrNilValue(2).as

            }
        } else {
            fail("Not an item with subprotocol")
        }
    } else {
        fail("Type should be Array")
    }
    return retVal
}


fun pack(protocol: Int, subProtocol: Int, message: Any): ByteArray {
    val byteArray = moshiPack.packToByteArray(listOf(
        protocol,
        subProtocol,
        message
        ))
    return byteArray
}

fun unpack(packedArray: ByteArray): Array<Any> {
    val unpacked: Array<Any> = moshiPack.unpack(packedArray)

    return unpacked
}


fun unpackProtocols(packedArray: ByteArray): Array<Any> {

    val unpacker = MessagePack.newDefaultUnpacker(packedArray)

//    return UntypedMessage(unpacker.get)

    val unpacked: Array<Any> = moshiPack.unpack(packedArray)

    return unpacked
}

//data class SomeObject(var compact: Boolean = true, var schema: Int = 0, var text: String = "hi")
data class UntypedMessage(val protocol: Int, val subProtocol: Int)

data class MessageObject(var protocol: Int? = null, var subProtocol: Int? = null, var message: Any? = null)

data class ProtocolMessage(var protocol: Int? = null, var subProtocol: Int? = null, var message: Value? = null)

data class MessageComplexSubObject(val protocol: Int, val subProtocol: Int, val message: ComplexSubObject)

data class ComplexSubObject(val someString: String, val someInt: Int, val someDouble: Double, val someLong: Long, val someBoolean: Boolean, val someList: List<Int>)