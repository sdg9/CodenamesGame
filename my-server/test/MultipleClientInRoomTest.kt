import com.gofficer.colyseus.server.Protocol
import com.gofficer.codenames.myServer.main
import io.ktor.application.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.server.testing.*
import org.junit.Rule
import org.junit.rules.Timeout
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.test.*

/**
 * Tests the [ChatApplication].
 */
class MultipleClientInRoomTest {

    @get:Rule
    val timeout = Timeout(5L, TimeUnit.SECONDS)

    // TODO sometimes this one passes
    @Test
    fun testTwoClientsCanJoinSameRoomUsingText() {
        withTestApplication(Application::main) {
            handleWebSocketConversation("/?useTextOverBinary=true") { serverIncoming, clientOutgoing ->
                println("============\nClient 1 connecting\n============")
                val id = step1GetIDFromServer(serverIncoming, clientOutgoing)
                step2ClientRequestRoomToJoin(serverIncoming, clientOutgoing)
                val roomId = step3ServerReturnRoomID(serverIncoming, clientOutgoing)
                val endpoint = getEndpoint(id, roomId, true)
                handleWebSocketConversation(endpoint) { roomIncoming, clientRoomOutgoing ->
                    step4ClientConnectToRoom(roomIncoming, clientRoomOutgoing, roomId)


                    handleWebSocketConversation("/?useTextOverBinary=true") { client2ServerIncoming, clinet2Outgoing ->
                        println("============\nClient 2 connecting\n============")
                        // Join 1 second later, just in case I currently have concurrency issues
//                        Thread.sleep(10)
                        val client2Id = step1GetIDFromServer(client2ServerIncoming, clinet2Outgoing)
                        step2ClientRequestRoomToJoin(client2ServerIncoming, clinet2Outgoing)
                        val client2RoomId = step3ServerReturnRoomID(client2ServerIncoming, clinet2Outgoing)
                        val client2Endpoint = getEndpoint(client2Id, client2RoomId, true)
                        handleWebSocketConversation(client2Endpoint) { client2RoomIncoming, client2RoomOutgoing ->
                            step4ClientConnectToRoom(client2RoomIncoming, client2RoomOutgoing, client2RoomId)

                            // Since both clients requested same type of room, it should be same id
                            assertEquals(roomId, client2RoomId)

                        }
                    }
                }
            }
        }
    }


    @Test
    fun testTwoClientsCanCommunicateThroughRoomUsingText() {
        withTestApplication(Application::main) {
            handleWebSocketConversation("/?useTextOverBinary=true") { serverIncoming, clientOutgoing ->
                println("============\nClient 1 connecting\n============")
                val id = step1GetIDFromServer(serverIncoming, clientOutgoing)
                step2ClientRequestRoomToJoin(serverIncoming, clientOutgoing)
                val roomId = step3ServerReturnRoomID(serverIncoming, clientOutgoing)
                val endpoint = getEndpoint(id, roomId, true)
                handleWebSocketConversation(endpoint) { client1RoomIncoming, client1RoomOutgoing ->
                    step4ClientConnectToRoom(client1RoomIncoming, client1RoomOutgoing, roomId)


                    handleWebSocketConversation("/?useTextOverBinary=true") { client2ServerIncoming, clinet2Outgoing ->
                        println("============\nClient 2 connecting\n============")
                        // Join 1 second later, just in case I currently have concurrency issues
//                        Thread.sleep(10)
                        val client2Id = step1GetIDFromServer(client2ServerIncoming, clinet2Outgoing)
                        step2ClientRequestRoomToJoin(client2ServerIncoming, clinet2Outgoing)
                        val client2RoomId = step3ServerReturnRoomID(client2ServerIncoming, clinet2Outgoing)
                        val client2Endpoint = getEndpoint(client2Id, client2RoomId, true)
                        handleWebSocketConversation(client2Endpoint) { client2RoomIncoming, client2RoomOutgoing ->
                            step4ClientConnectToRoom(client2RoomIncoming, client2RoomOutgoing, client2RoomId)
                            assertEquals(roomId, client2RoomId)
                            println("==================\nBoth Clients in room\n==================")

                            // Since both clients requested same type of room, it should be same id

//                            client1RoomOutgoing.send(Frame.Text(toJSON(SomethingElse("test"))))

                            // TODO fix me, this logic doens't exist yet
                            val byteArray = moshiPack.packToByteArray(listOf(Protocol.ROOM_DATA, 2, "Some message"))


                            val client1ResponseAfterClient1Sends =
                                (client1RoomIncoming.receive() as Frame.Binary).readBytes()

//                            moshiPack.unpack<>(client1ResponseAfterClient1Sends)

                            client1RoomOutgoing.send(Frame.Binary(true, ByteBuffer.wrap(byteArray)))

//                            val client1ResponseAfterClient1Sends =
//                                (client1RoomIncoming.receive() as Frame.Binary).readText()
//
//                            val client2ResponseAfterClient1Sends =
//                                (client2RoomIncoming.receive() as Frame.Binary).readText()
//
//                            println("Client 1 gets $client1ResponseAfterClient1Sends")
//                            println("Client 2 gets $client2ResponseAfterClient1Sends")
//                            assertNotNull(client2ResponseAfterClient1Sends)
//                            assertEquals(client1ResponseAfterClient1Sends, client2ResponseAfterClient1Sends)

                        }
                    }
                }
            }
        }
    }
}