package com.example


import com.daveanthonythomas.moshipack.MoshiPack
import com.example.common.*
import okio.BufferedSource
import org.msgpack.core.MessagePack
import org.msgpack.value.ValueType
import kotlin.test.*
import org.apache.commons.codec.binary.Hex;
import org.msgpack.core.MessageUnpacker

/**
 * Testing out how to conveniently pack and unpack unknown types using message pack
 *
 * Notes
 * Whenever not explicitly typed, Moshi packing/unpacking will convert any number to double
 *
 * May be easier to always work with doubles in this regard
 */
class MessagePackBetweenClientAndServer {


    /**
     * Ints and strings
     */
    @Test
    fun packAndUnpackKnownTypes1() {
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

    /**
     * Adds in map
     */
    @Test
    fun packAndUnpackKnownTypes2() {
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
    }

    /**
     * Adds in complex type
     */
    @Test
    fun packAndUnpackKnownTypes3() {
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

    /**
     * Provided subtype code is indicative of class to use, illustrates how to unpack an item not known until
     * subtype is available
     */
    @Test
    fun packAndUnpackUnknown1() {
        // Pack
        val protocol = Protocol.ROOM_DATA
        val subProtocol = 2
        val anyObj = ComplexSubObject("hi", 1, 1.0, 3L, true, listOf(1, 2, 3))
        val packed: BufferedSource = moshiPack.pack(listOf(protocol, subProtocol, anyObj))
        val packedByteArray = packed.readByteArray()
        if (packedByteArray == null) {
            fail("Packed byte array cannot be null")
            return
        }

        // Print
        println()
        println(Hex.encodeHexString(packedByteArray))

        // Unpack
        val someObject = unpackUnknown(packedByteArray)
//        930d0286aa736f6d65537472696e67a26869a7736f6d65496e7401aa736f6d65446f75626c6501a8736f6d654c6f6e6703ab736f6d65426f6f6c65616ec3a8736f6d654c69737493010203
        assertEquals(protocol, someObject?.protocol)
        assertEquals(subProtocol, someObject?.subProtocol)


        val byteArrayMesage = someObject?.message
        assertEquals(
            "86aa736f6d65537472696e67a26869a7736f6d65496e7401aa736f6d65446f75626c6501a8736f6d654c6f6e6703ab736f6d65426f6f6c65616ec3a8736f6d654c69737493010203",
            Hex.encodeHexString(byteArrayMesage)
        )

        if (byteArrayMesage != null && someObject.subProtocol == 2) {
            // Given we know type and subtype, we can deduce which class to unpack to
            // That is the same class should be used for all subtypes
            val unpacked: ComplexSubObject = moshiPack.unpack(byteArrayMesage)
            assertEquals(anyObj, unpacked)
        } else {
            fail("Message must not be null or unsupported subProtocol")
        }
    }

    // TODO add test where item doesn't conform to protocol (don't crash)
    @Test
    fun invalidFormatUnpacksNull1() {
        val packedByteArray =  moshiPack.pack(listOf(1)).readByteArray()
        val unpackedObject = unpackUnknown(packedByteArray)

        assertNull(unpackedObject)
    }

    @Test
    fun invalidFormatUnpacksNull2() {
        val packedByteArray =  moshiPack.pack(listOf(1, 2, 3, 4)).readByteArray()
        val unpackedObject = unpackUnknown(packedByteArray)

        assertNull(unpackedObject)
    }

    @Test
    fun invalidFormatUnpacksNull3() {
        val packedByteArray =  moshiPack.pack(listOf(1, "string", 3)).readByteArray()
        val unpackedObject = unpackUnknown(packedByteArray)

        assertNull(unpackedObject)
    }

    // TODO add test where item doesn't have message, (don't crash on bad index)
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

//        if (length == 2 || length == 3) {
//            val firstArrayItem = unpacker.nextFormat
//            if (firstArrayItem.valueType == ValueType.INTEGER) {
////                println("First bytes: ${Hex.encodeHexString(unpacker.readPayload(1))}")
//                // 0d = 13 (good)
//                retVal.protocol = unpacker.unpackInt()
//            } else {
//                fail("First item must be an integer")
//            }
//            val secondArrayItem = unpacker.nextFormat
//            if (secondArrayItem.valueType == ValueType.INTEGER) {
////                println("Second bytes: ${Hex.encodeHexString(unpacker.readPayload(1))}")
//                // 02 = 2 (good)
//                retVal.subProtocol = unpacker.unpackInt()
//            }
//            val thirdArrayItem = unpacker.nextFormat
//            if (thirdArrayItem.valueType == ValueType.MAP) {
//
//                println("Total read: ${unpacker.totalReadBytes}")
//
//                // Only works when this is the last item, should be sufficient for what I need
//                val remainderUnreadBytes = packed.size - unpacker.totalReadBytes.toInt()
//                retVal.message = unpacker.readPayload(remainderUnreadBytes)
//            }
//        } else {
//            fail("Arrays can only have 2 or 3 items")
//        }
//    } else {
//        fail("Type should be Array")
//    }
//    return retVal
}


fun unpackLength2(unpacker: MessageUnpacker, totalSize: Int): ProtocolMessage {
    val retVal = ProtocolMessage()
    val firstArrayItem = unpacker.nextFormat
    if (firstArrayItem.valueType == ValueType.INTEGER) {
        retVal.protocol = unpacker.unpackInt()
    } else {
        fail("First item must be an integer")
    }
    val remainderUnreadBytes = totalSize - unpacker.totalReadBytes.toInt()
    retVal.message = unpacker.readPayload(remainderUnreadBytes)
    return retVal
}

fun unpackLength3(unpacker: MessageUnpacker, totalSize: Int): ProtocolMessage {
    val retVal = ProtocolMessage()

    val firstArrayItem = unpacker.nextFormat
    if (firstArrayItem.valueType == ValueType.INTEGER) {
        retVal.protocol = unpacker.unpackInt()
    } else {
        fail("First item must be an integer")
    }
    val secondArrayItem = unpacker.nextFormat
    if (secondArrayItem.valueType == ValueType.INTEGER) {
        retVal.subProtocol = unpacker.unpackInt()
    }
    val thirdArrayItem = unpacker.nextFormat
    if (thirdArrayItem.valueType == ValueType.MAP) {

        println("Total read: ${unpacker.totalReadBytes}")

        // Only works when this is the last item, should be sufficient for what I need
        val remainderUnreadBytes = totalSize - unpacker.totalReadBytes.toInt()
        retVal.message = unpacker.readPayload(remainderUnreadBytes)
    }
    return retVal
}

//data class SomeObject(var compact: Boolean = true, var schema: Int = 0, var text: String = "hi")
data class UntypedMessage(val protocol: Int, val subProtocol: Int)

data class MessageObject(var protocol: Int? = null, var subProtocol: Int? = null, var message: Any? = null)

data class ProtocolMessage(
    var protocol: Int? = null,
    var subProtocol: Int? = null,
    var message: ByteArray? = null
)

data class MessageComplexSubObject(val protocol: Int, val subProtocol: Int, val message: ComplexSubObject)

data class ComplexSubObject(
    val someString: String,
    val someInt: Int,
    val someDouble: Double,
    val someLong: Long,
    val someBoolean: Boolean,
    val someList: List<Int>
)