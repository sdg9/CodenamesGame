
import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.colyseus.network.Protocol
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.nio.ByteBuffer

var moshiPack = MoshiPack()

// Step 1: Server sends user ID to client on connection
suspend fun step1GetIDFromServer(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>): String {
    println("Step 1")
//    [1,"9DJlP0XN7"]
    val bytes = (serverIncoming.receive() as Frame.Binary).readBytes()

    val plug: Array<Any> = moshiPack.unpack(bytes)

    val id = plug.get(1)

    return id as String
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
    val plug: Array<Any> = moshiPack.unpack(roomCreatedMessage)


    val roomId = plug.get(1)

    assertNotNull(roomId)
    return roomId as String
}

// Step 4 Connect to game room
suspend fun step4ClientConnectToRoom(serverIncoming: ReceiveChannel<Frame>, clientOutgoing: SendChannel<Frame>, roomId: String) {
    println("Step 4")
    // Game server will return connection and then game state
    // [10,"gnoLgHxDv"]
    // [14,{"board":{"cards":[]},"lastPlayed":0,"gameOver":false,"cards":[{"id":1,"text":"STAR","type":"BLUE","isRevealed":false},{"id":2,"text":"PUMPKIN","type":"BLUE","isRevealed":false},{"id":3,"text":"DRAGON","type":"RED","isRevealed":false},{"id":4,"text":"NET","type":"BYSTANDER","isRevealed":false},{"id":5,"text":"ROW","type":"RED","isRevealed":false},{"id":6,"text":"SUPERHERO","type":"RED","isRevealed":false},{"id":7,"text":"SCHOOL","type":"RED","isRevealed":false},{"id":8,"text":"NEW YORK","type":"BYSTANDER","isRevealed":false},{"id":9,"text":"MOUSE","type":"BYSTANDER","isRevealed":false},{"id":10,"text":"EUROPE","type":"BLUE","isRevealed":false},{"id":11,"text":"HOOD","type":"RED","isRevealed":false},{"id":12,"text":"DOCTOR","type":"RED","isRevealed":false},{"id":13,"text":"CENTER","type":"BLUE","isRevealed":false},{"id":14,"text":"PLATYPUS","type":"BYSTANDER","isRevealed":false},{"id":15,"text":"REVOLUTION","type":"BYSTANDER","isRevealed":false},{"id":16,"text":"BOMB","type":"RED","isRevealed":false},{"id":17,"text":"CAR","type":"BLUE","isRevealed":false},{"id":18,"text":"OPERA","type":"DOUBLE_AGENT","isRevealed":false},{"id":19,"text":"TAP","type":"BLUE","isRevealed":false},{"id":20,"text":"PUPIL","type":"RED","isRevealed":false},{"id":21,"text":"PLANE","type":"BYSTANDER","isRevealed":false},{"id":22,"text":"BUFFALO","type":"BLUE","isRevealed":false},{"id":23,"text":"CASINO","type":"BLUE","isRevealed":false},{"id":24,"text":"SCUBA DIVER","type":"BYSTANDER","isRevealed":false},{"id":25,"text":"RACKET","type":"BLUE","isRevealed":false}]}]
    val roomJoinConfirmationMessage = (serverIncoming.receive() as Frame.Binary).readBytes()
    val plug: Array<Any> = moshiPack.unpack(roomJoinConfirmationMessage)

    // TODO sometimes this errors out, i think order of reply may change
    val confirmedRoomId = plug.get(1) as String
    println("4.1 <-- Confirming room id $confirmedRoomId")
    assertEquals(confirmedRoomId, roomId)

    val initialRoomStateMessage = (serverIncoming.receive() as Frame.Binary).readBytes()
    assertNotNull(initialRoomStateMessage)

    val roomStateUnpacked: Array<Any> = moshiPack.unpack(initialRoomStateMessage)
    println("4.2 <-- Confirming game state is sent ${roomStateUnpacked.get(1)}")
    assertEquals(Protocol.ROOM_STATE.toDouble(), roomStateUnpacked.get(0))

}

fun getEndpoint(id: String, roomId: String): String {
    var endpoint = "/$roomId?colyseusid=$id&requestId=1"
    println("Connecting to $endpoint")
    return endpoint
}