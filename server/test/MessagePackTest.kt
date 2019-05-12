package com.example


import com.daveanthonythomas.moshipack.MoshiPack
import com.example.common.*
import okio.BufferedSource
import okio.ByteString
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

//        println("Total bytes: ${packed.}")
        println(packed.readByteString().hex())

//
//        val originalMessage = UserConnected("1234","User1")
//        val json = toJSON(originalMessage)
//
//        assertEquals(
//            "{\"id\":\"1234\",\"name\":\"User1\",\"type\":\"USER_CONNECTED\"}", json
//        )
//
//        val parsedMessage = fromJson<UserConnected>(json)
//        assertEquals(
//            originalMessage, parsedMessage
//        )
    }

    @Test
    fun convertMessagePackToString() {
        val bytes = ByteString.decodeHex("82a7636f6d70616374c3a6736368656d6100").toByteArray()

        val moshiPack = MoshiPack()
        val plug: MessagePackWebsitePlug = moshiPack.unpack(bytes)


        println(plug)
    }
}