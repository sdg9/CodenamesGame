
import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.colyseus.server.*
import com.gofficer.codenames.redux.actions.ActionType
import com.gofficer.codenames.redux.actions.UserConnected
//import com.gofficer.codenames.redux.actions.getActionTypeFromJson
//import com.gofficer.codenames.redux.actions.getMoshiBuilder
import okio.BufferedSource
import okio.ByteString
import kotlin.test.*


/**
 * Tests the [SomeApplication].
 */
class MessageParsingTest {

//    interface SocketMessage {
//        val protocol: Int
//    }
//
//    interface com.gofficer.codenames.redux.Action {
//        val type: String
//        val payload: Any
//    }
//    data class TestMessage(override val protocol: Int, val message: String): SocketMessage
//
//    data class SocketActionMessage(override val protocol: Int,
//                                   override val type: String,
//                                   override val payload: Any
//    ) : SocketMessage, com.gofficer.codenames.redux.Action
//
//    data class NestedObject(val someString: String = "String", val someNumber: Int = 1)


//    data class TestComplexObject(val protocol: Int, val String)
    /**
     * This is an integration test that verifies the behaviour of a simple conversation with an empty server.
     */
//    @Test
//    fun testUserConnectedJSON() {
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
//    }

//    @Test
//    fun testParseJSONWithoutKnowingType() {
//        val json = "{\"name\":\"User1\",\"type\":\"USER_CONNECTED\"}"
//        val type = getActionTypeFromJson(json)
//
//        assertEquals(
//            type, ActionType.USER_CONNECTED
//        )
//    }
//
//    @Test
//    fun testParseJSONHandlingUnknownType() {
//        val json = "{\"name\":\"User1\",\"type\":\"SOME_UNKNOWN_TYPE\"}"
//        val type = getActionTypeFromJson(json)
//
//        assertEquals(
//            type, null
//        )
//    }

    @Test
    fun testUserConnectedMessagePack() {

        val originalMessage = UserConnected("1234","User1")
        val parsedMessage = packAndUnpack(originalMessage)

        assertEquals(
            originalMessage, parsedMessage
        )

    }

//    @Test
//    fun testMessageParsingUsingBasicSocketAction() {
//        val message = SocketActionMessage(Protocol.USER_ID, type = "SomeType", payload = "SomePayload")
//        val moshi = Moshi.Builder().build()
//        val jsonAdapter = moshi.adapter(SocketActionMessage::class.java)
//        val json = jsonAdapter.toJson(message)
//
//        assertEquals(
//            json,
//            "{\"payload\":\"SomePayload\",\"protocol\":1,\"type\":\"SomeType\"}"
//        )
//    }
//
//    @Test
//    fun testMessageParsingUsingBasicSocketActionToAndFromJSON() {
//        val message = SocketActionMessage(Protocol.USER_ID, type = "SomeType", payload = "SomePayload")
//        val message2 = toAndFromJSON(message)
//
//        assertEquals(
//            message,
//            message2
//        )
//    }
//
//
//    @Test
//    fun testMessageParsingUsingBasicSocketActionToAndFromJSON2() {
//        val message = SocketActionMessage(Protocol.USER_ID, type = "SomeType", payload = listOf("String1", "String2"))
//        val message2 = toAndFromJSON(message)
//
//        assertEquals(
//            message,
//            message2
//        )
//    }

    // parses out but will need assistance
//    @Test
//    fun testMessageParsingUsingBasicSocketActionToAndFromJSON3() {
//        val message = SocketActionMessage(Protocol.USER_ID, type = "SomeType", payload = listOf(NestedObject(), NestedObject()))
//        val message2 = toAndFromJSON(message)
//
//        assertEquals(
//            message,
//            message2
//        )
//    }

//
//    @Test
//    fun testMessageParsingUsingComplexSocketAction() {
//
//        val message = SocketActionMessage(
//            Protocol.USER_ID,
//            type = "SomeType",
//            payload = linkedMapOf("key1" to "value1", "key2" to "value2")
//        )
//        val moshi = Moshi.Builder().build()
//        val jsonAdapter = moshi.adapter(SocketActionMessage::class.java)
//        val json = jsonAdapter.toJson(message)
//
//        assertEquals(
//            json,
//            "{\"payload\":{\"key1\":\"value1\",\"key2\":\"value2\"},\"protocol\":1,\"type\":\"SomeType\"}"
//        )
//
//    }
//
//    @Test
//    fun testMessageParsingToAndFromJSON() {
//
//        val message = SocketActionMessage(
//            Protocol.USER_ID,
//            type = "SomeType",
//            payload = linkedMapOf("key1" to "value1", "key2" to "value2")
//        )
//        val message2 = toAndFromJSON(message)
//
//        assertEquals(
//            message,
//            message2
//        )
//    }
//
//    @Test
//    fun testMessageParsingUsingMessagePack() {
//        val message = TestMessage(Protocol.USER_ID, "Welcome")
//
//        val moshiPack = MoshiPack()
//        val packed: BufferedSource = moshiPack.pack(message)
//
//        val byteStringHex = packed.readByteString().hex()
//        assertEquals(
//            "82a870726f746f636f6c01a76d657373616765a757656c636f6d65", byteStringHex
//        )
//
//        val bytes = ByteString.decodeHex(byteStringHex).toByteArray()
//        val message2: TestMessage = moshiPack.unpack(bytes)
//
//        assertEquals(
//            message, message2
//        )
//
//    }
//
//    @Test
//    fun testMessageParsingUsingBasicSocketActionMessagePack() {
//        val message = SocketActionMessage(Protocol.USER_ID, type = "SomeType", payload = "SomePayload")
//        val message2 = packAndUnpack(message)
//
//        assertEquals(
//            message, message2
//        )
//    }
//
//    @Test
//    fun testMessageParsingUsingComplexSocketActionMessagePack() {
//        val message = SocketActionMessage(
//            Protocol.USER_ID,
//            type = "SomeType",
//            payload = linkedMapOf("key1" to "value1", "key2" to "value2")
//        )
//        val message2 = packAndUnpack(message)
//
//        assertEquals(
//            message, message2
//        )
//    }

//    @Test
//    fun unknownButValidFormat() {
//        val message = "{\"payload\":\"user1\",\"protocol\":1,\"type\":\"USER_CONNECTED\"}"
//
//        val moshi = Moshi.Builder().build()
//        val jsonAdapter = moshi.adapter(SocketAction::class.java)
//        val json = jsonAdapter.fromJson(message)
//
//        assertEquals(
//            "USER_CONNECTED",
//            json?.type
//        )
//        assertEquals(
//            1,
//            json?.protocol
//        )
//        assertEquals(
//            "user1",
//            json?.payload
//        )
//
//    }

    private inline fun <reified T> packAndUnpack(message: T): T {
        val moshiPack = MoshiPack()
        val packed: BufferedSource = moshiPack.pack(message)

        val byteStringHex = packed.readByteString().hex()

        val bytes = ByteString.decodeHex(byteStringHex).toByteArray()
        return moshiPack.unpack(bytes)
    }


//    private inline fun <reified T> toJSON(message: T): String {
//        val jsonAdapter = getMoshiBuilder().adapter(T::class.java)
//        return jsonAdapter.toJson(message)
//    }
//
//    private inline fun <reified T> fromJson(json: String): T? {
//        val jsonAdapter = getMoshiBuilder().adapter(T::class.java)
//        return jsonAdapter.fromJson(json)
//    }

//    @FromJson
//    fun fromJson(reader: JsonReader, newFollowerAdapter: JsonAdapter<NewFollower>): Content {
//        val value = reader.readJsonValue() as Map<String, Any>
//        // TODO: Deserialize.
//    }
}