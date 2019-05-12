import com.example.common.getAttributeFromActionJson
import com.squareup.moshi.Moshi
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

val moshi = Moshi.Builder().build()
val jsonAdapter = moshi.adapter(Map::class.java)

// Step 1: Server sends user ID to client on connection
suspend fun step1GetIDFromServer(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>): String {
    println("Step 1")
    val serverProvideIdMessage = (serverIncoming.receive() as Frame.Text).readText()
    val id = getAttributeFromActionJson(serverProvideIdMessage,"id") as String
    println("1 <-- $serverProvideIdMessage")
//    println("Server provides id $id [$serverProvideIdMessage]")
    assertNotNull(id)
    return id
}

// Step 2 Client responds with desired room
suspend fun step2ClientRequestRoomToJoin(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>) {
    println("Step 2")
    val requestedRoom = "public"
    val clientHandShake = mapOf("requestId" to 1, "room" to requestedRoom, "type" to "JOIN_REQUEST")
    val clientMessage = Frame.Text(jsonAdapter.toJson(clientHandShake))
    println("2 --> ${jsonAdapter.toJson(clientHandShake)}")
    clientOutgoing.send(clientMessage)
}

// Step 3 Server creates room and responds with room ID
suspend fun step3ServerReturnRoomID(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>): String {
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
    // Server also should return initial room state
    val initialRoomStateMessage = (serverIncoming.receive() as Frame.Text).readText()
    assertNotNull(initialRoomStateMessage)
    println("4.1 <-- $initialRoomStateMessage")

    val roomJoinConfirmationMessage = (serverIncoming.receive() as Frame.Text).readText()
    println("4.2 <-- $roomJoinConfirmationMessage")
    val confirmedRoomId = getAttributeFromActionJson(roomJoinConfirmationMessage,"roomId") as String
    assertEquals(confirmedRoomId, roomId)
}

fun getEndpoint(id: String, roomId: String): String {
    val endpoint = "/room/$roomId?colyseusid=$id&requestId=1"
    println("Connecting to $endpoint")
    return endpoint
}