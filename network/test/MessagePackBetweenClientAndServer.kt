

import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.colyseus.network.Protocol
import com.gofficer.colyseus.network.unpackUnknown
import junit.framework.TestCase.*
import okio.BufferedSource
import org.apache.commons.codec.binary.Hex;
import org.junit.Test

/**
 * Testing out how to conveniently pack and unpack unknown types using message pack
 *
 * Notes
 * Whenever not explicitly typed, Moshi packing/unpacking will convert any number to double
 *
 * May be easier to always work with doubles in this regard
 */
class MessagePackBetweenClientAndServer {

    val moshiPack = MoshiPack()

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
        val packedByteArray: ByteArray = moshiPack.packToByteArray(listOf(protocol, subProtocol, anyObj))

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
        val packedByteArray =  moshiPack.packToByteArray(listOf(1))
        val unpackedObject = unpackUnknown(packedByteArray)

        assertNull(unpackedObject)
    }

    @Test
    fun invalidFormatUnpacksNull2() {
        val packedByteArray =  moshiPack.packToByteArray(listOf(1, 2, 3, 4))
        val unpackedObject = unpackUnknown(packedByteArray)

        assertNull(unpackedObject)
    }

//    @Test
//    fun invalidFormatUnpacksNull3() {
//        val packedByteArray =  moshiPack.pack(listOf(1, "string", 3)).readByteArray()
//        val unpackedObject = unpackUnknown(packedByteArray)
//
//        assertNull(unpackedObject)
//    }

    // TODO add test where item doesn't have message, (don't crash on bad index)
}

data class MessageObject(var protocol: Int? = null, var subProtocol: Int? = null, var message: Any? = null)

data class MessageComplexSubObject(val protocol: Int, val subProtocol: Int, val message: ComplexSubObject)

data class ComplexSubObject(
    val someString: String,
    val someInt: Int,
    val someDouble: Double,
    val someLong: Long,
    val someBoolean: Boolean,
    val someList: List<Int>
)