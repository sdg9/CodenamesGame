
import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.codenames.myServer.main
import com.gofficer.codenames.redux.actions.TouchCard
import com.gofficer.colyseus.network.Protocol
import com.gofficer.colyseus.network.SubProtocol
import com.gofficer.colyseus.network.pack
import com.gofficer.colyseus.myServer.test.*
import com.squareup.moshi.Moshi
import io.ktor.application.*
import io.ktor.server.testing.*
import org.junit.Rule
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import org.apache.commons.codec.binary.Hex
import java.nio.ByteBuffer

/**
 * Tests the [ChatApplication].
 */
class SingleClientGame {
    val moshiPack = MoshiPack()

    // TODO: Initial connect http://localhost:2567/?colyseusid=
    // TODO: subsequent connect http://localhost:2567/zk2-pgKfC?colyseusid=uoZN4BhzJ&requestId=1

    @get:Rule
    val timeout = Timeout(5L, TimeUnit.SECONDS)

    /**
     * Server
     *
     * Server connection response
     * [
    1,
    "4x83UPDqX"
    ]


    Client respnose
    [
    10,
    "public",
    {
    "requestId": 1
    }
    ]

    Server response
    [
    10,
    "zk2-pgKfC",
    1
    ]

     *
     *
     *
     * Room http://localhost:2567/zk2-pgKfC?colyseusid=4x83UPDqX&requestId=1
     *
     * First room message
     * [
    0,
    "ZcM9b34A-"
    ]

    Then heavy data is passed
     */
    // leave and come back

    // http://localhost:2567/?colyseusid=
    // http://localhost:2567/zk2-pgKfC?colyseusid=4x83UPDqX&requestId=1

    /**
     * Room stays open as long as it's not full
     * Both sessions continually maintained (is this required/helpful?)
     */

    val moshi = Moshi.Builder().build()
    val jsonAdapter = moshi.adapter(Map::class.java)

    @Test
    fun testServerProvidesIdIfEmptyUsingText() {
        withTestApplication(Application::main) {
            handleWebSocketConversation("/") { serverIncoming, clientOutgoing ->
                val id = step1GetIDFromServer(serverIncoming, clientOutgoing)
                assertNotNull(id)
            }
        }
    }

    @Test
    fun testServerDoesNotProvideIdIfSpecified() {
        withTestApplication(Application::main) {
            val id = "1234"
            handleWebSocketConversation("/?colyseusid=$id") { serverIncoming, clientOutgoing ->
                step2ClientRequestRoomToJoin(serverIncoming, clientOutgoing)
                val roomId = step3ServerReturnRoomID(serverIncoming, clientOutgoing)
                println("====Room ID: $roomId")
                val endpoint = getEndpoint(id, roomId)

                println("====Endpoint: $endpoint")
                handleWebSocketConversation(endpoint) { roomIncoming, clientRoomOutgoing ->
                    step4ClientConnectToRoom(roomIncoming, clientRoomOutgoing, roomId)

                    // By here game state is sent to client

                    // Send pressed event
                    val byteArray = pack(Protocol.ROOM_DATA, SubProtocol.TOUCH_CARD, TouchCard(1))

                    val pattern: Regex = "(\\S{2})".toRegex()
//                    println()
//                    println(Hex.encodeHexString(packedByteArray).replace(pattern, "$1 "))
                    println("String hex: ${Hex.encodeHexString(byteArray).replace(pattern, "$1 ")}")
                    val clientMessage = Frame.Binary(true, ByteBuffer.wrap(byteArray))
                    clientRoomOutgoing.send(clientMessage)
                    // Confirm state is updated accordingly
//
                    val serverUpdate = (roomIncoming.receive() as Frame.Binary).readBytes()
                    val unpacked: Array<Any> = moshiPack.unpack(serverUpdate)
                    println("Unpacked; ${unpacked.get(2)}")

                }
            }
        }
    }
}