package com.example


import com.daveanthonythomas.moshipack.MoshiPack
import com.example.common.*
import okio.BufferedSource
import okio.ByteString
import org.apache.commons.codec.binary.Hex
import org.msgpack.core.MessagePack
import org.msgpack.value.ValueType
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.test.*


/**
 * Tests the [SomeApplication].
 */
class MessagePackTest {

    data class MessagePackWebsitePlug(var compact: Boolean = true, var schema: Int = 0)

    @Test
    fun convertToMessagePack() {

        val moshiPack = MoshiPack()
        val packed: BufferedSource = moshiPack.pack(MessagePackWebsitePlug())

        println(packed.readByteString().hex())
    }

    @Test
    fun convertMessagePackToString() {
        val bytes = ByteString.decodeHex("82a7636f6d70616374c3a6736368656d6100").toByteArray()

        val moshiPack = MoshiPack()
        val plug: MessagePackWebsitePlug = moshiPack.unpack(bytes)


        println(plug)
    }

    @Test
    fun packVariousWays() {

        val bytes = MoshiPack().packToByteArray(MessagePackWebsitePlug())
        val bytes2 = ByteString.decodeHex("82a7636f6d70616374c3a6736368656d6100").toByteArray()
        val bytes3 = MoshiPack().packToByteArray(MessagePackWebsitePlug())


//        println("Hex Bytes: ${Hex.encodeHex( bytes2 ).toString()}")
//        assertNotEquals(bytes, bytes3)

        val unpacked1: MessagePackWebsitePlug = MoshiPack().unpack(bytes)
//        println(unpacked1)

        val unpacked2: MessagePackWebsitePlug = MoshiPack().unpack(bytes2)
//        val unpacked2: MessagePackWebsitePlug = MoshiPack().unpack(bytes2)
//        println(unpacked2)
        val unpacked3: MessagePackWebsitePlug = MoshiPack().unpack(bytes3)
//        println(unpacked3)

        assertEquals(unpacked1, unpacked2)
        assertEquals(unpacked1, unpacked3)
    }

    @Test
    fun whyDoByteArraysNotEqual() {
        // TODO understand why this is the case where arrays don't equal
        val someHexString = "82a7636f6d70616374c3a6736368656d6100"
        val bytes1 = ByteString.decodeHex(someHexString).toByteArray()
        val bytes2 = ByteString.decodeHex(someHexString).toByteArray()

        // Not sure why but byte arrays are not equal or same despite
        assertNotSame(bytes1, bytes2)
        assertNotEquals(bytes1, bytes2)
    }


    @Test
    fun byteArrayManipulation() {
        val someHexString = "82a7636f6d70616374c3a6736368656d6100"
        val bytes = ByteString.decodeHex(someHexString).toByteArray()
        val unpackedObject1: MessagePackWebsitePlug = MoshiPack().unpack(bytes)

        val packedByteArray = packProtocol(Protocol.USER_ID, bytes)

        // send over socket

        val unpacedBytes = unpackProtocol(packedByteArray)
        assertEquals(unpacedBytes.protocol, Protocol.USER_ID)
        val unpackedObject2: MessagePackWebsitePlug = MoshiPack().unpack(unpacedBytes.byteArray)
        assertEquals(unpackedObject1, unpackedObject2)
    }
}

fun packProtocol(protocol: Int, bytes: ByteArray): ByteArray {
    val outputStream = ByteArrayOutputStream()
    outputStream.write(protocol)
    outputStream.write(bytes)
    return outputStream.toByteArray()
}

data class DecryptProtocol(val protocol: Int, val byteArray: ByteArray)

fun unpackProtocol(bytes: ByteArray): DecryptProtocol {
    val protocol = bytes[0].toInt()
    val newByteArray = Arrays.copyOfRange(bytes, 1, bytes.size)
    return DecryptProtocol(protocol, newByteArray)
}
