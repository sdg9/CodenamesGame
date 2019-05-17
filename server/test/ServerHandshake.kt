
import com.daveanthonythomas.moshipack.MoshiPack
import com.example.common.Protocol
import com.gofficer.codenames.redux.actions.getAttributeFromActionJson
import com.squareup.moshi.Moshi
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.nio.ByteBuffer

val moshi = Moshi.Builder().build()
val jsonAdapter = moshi.adapter(Map::class.java)
var moshiPack = MoshiPack()

// Step 1: Server sends user ID to client on connection
suspend fun step1GetIDFromServer(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>): String {
    println("Step 1")
//    [1,"9DJlP0XN7"]
    val bytes = (serverIncoming.receive() as Frame.Binary).readBytes()

    val moshiPack = MoshiPack()
    val plug: Array<Any> = moshiPack.unpack(bytes)

    val id = plug.get(1)

    return id as String
}

suspend fun step1GetIDFromServerOldText(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>): String {
    println("Step 1")
//    [1,"9DJlP0XN7"]
    val serverProvideIdMessage = (serverIncoming.receive() as Frame.Text).readText()
    val id = getAttributeFromActionJson(serverProvideIdMessage,"id") as String
    println("1 <-- $serverProvideIdMessage")
//    println("Server provides id $id [$serverProvideIdMessage]")
    assertNotNull(id)
    return id
}

suspend fun step2ClientRequestRoomToJoinOldText(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>) {
    println("Step 2")
//    [10,"public",{"requestId":1}]
    val requestedRoom = "public"
    val clientHandShake = mapOf("requestId" to 1, "room" to requestedRoom, "type" to "JOIN_REQUEST")
    val clientMessage = Frame.Text(jsonAdapter.toJson(clientHandShake))
    println("2 --> ${jsonAdapter.toJson(clientHandShake)}")
    clientOutgoing.send(clientMessage)
}

// Step 2 Client responds with desired room
suspend fun step2ClientRequestRoomToJoin(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>) {
    println("Step 2")
//    [10,"public",{"requestId":1}]
    val requestedRoom = "public"
    val clientHandshake = moshiPack.packToByteArray(listOf(
        Protocol.JOIN_ROOM,
        requestedRoom,
        1
    ))
    val clientMessage = Frame.Binary(true, ByteBuffer.wrap(clientHandshake))
    println("2 --> Byte array")
    clientOutgoing.send(clientMessage)
}

// Step 3 Server creates room and responds with room ID
suspend fun step3ServerReturnRoomID(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>): String {
    println("Step 3")
//    [10,"gnoLgHxDv",1]
    val roomCreatedMessage = (serverIncoming.receive() as Frame.Binary).readBytes()
    assertNotNull(roomCreatedMessage)
    println("3 <-- $roomCreatedMessage")
    val moshiPack = MoshiPack()
    val plug: Array<Any> = moshiPack.unpack(roomCreatedMessage)


    val roomId = plug.get(1)

    assertNotNull(roomId)
    return roomId as String
}

// Step 3 Server creates room and responds with room ID
suspend fun step3ServerReturnRoomIDOldText(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>): String {
    println("Step 3")
    val roomCreatedMessage = (serverIncoming.receive() as Frame.Text).readText()
    assertNotNull(roomCreatedMessage)
    println("3 <-- $roomCreatedMessage")
    val roomId = getAttributeFromActionJson(roomCreatedMessage,"roomId") as String
    assertNotNull(roomId)

    return roomId
}

// Step 4 Connect to game room
suspend fun step4ClientConnectToRoom(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>, roomId: String) {
    println("Step 4")
    // [10,"gnoLgHxDv"]
    // Server also should return initial room state
//    val initialRoomStateMessage = (serverIncoming.receive() as Frame.Binary).readBytes()
//    assertNotNull(initialRoomStateMessage)

    val roomJoinConfirmationMessage = (serverIncoming.receive() as Frame.Binary).readBytes()
//    val roomJoinConfirmationMessage = (serverIncoming.receive() as Frame.Text).readText()
    println("4.2 <-- $roomJoinConfirmationMessage")
    val moshiPack = MoshiPack()
    val plug: Array<Any> = moshiPack.unpack(roomJoinConfirmationMessage)

    val confirmedRoomId = plug.get(1) as String
    println("Confirmed ID: $confirmedRoomId")
//    val confirmedRoomId = getAttributeFromActionJson(roomJoinConfirmationMessage,"roomId") as String
    assertEquals(confirmedRoomId, roomId)
}

suspend fun step4ClientConnectToRoomOldText(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>, roomId: String) {
    println("Step 4")
    // [10,"gnoLgHxDv"]
    // Server also should return initial room state
    val initialRoomStateMessage = (serverIncoming.receive() as Frame.Text).readText()
    assertNotNull(initialRoomStateMessage)
    println("4.1 <-- $initialRoomStateMessage")

    val roomJoinConfirmationMessage = (serverIncoming.receive() as Frame.Text).readText()
    println("4.2 <-- $roomJoinConfirmationMessage")
    val confirmedRoomId = getAttributeFromActionJson(roomJoinConfirmationMessage,"roomId") as String
    assertEquals(confirmedRoomId, roomId)
}

fun getEndpoint(id: String, roomId: String, useTextOverBinary: Boolean = false): String {
    var endpoint = "/$roomId?colyseusid=$id&requestId=1"
    if (useTextOverBinary) {
//        endpoint += "&useTextOverBinary=true"
    }
    println("Connecting to $endpoint")
    return endpoint
}