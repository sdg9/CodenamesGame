import com.example.main
import com.squareup.moshi.Moshi
import io.ktor.application.Application
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.withTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull

/**
 * Tests the [ChatApplication].
 */
class BinaryHandshake {

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

//    @Test
//    fun testUseTextOverBinary() {
//        withTestApplication(Application::main) {
//            handleWebSocketConversation("/?useTextOverBinary=true") { serverIncoming, clientOutgoing ->
//                //                val id = step1GetIDFromServer(serverIncoming, clientOutgoing)
////                assertNotNull(id)
//
//                val serverProvideIdMessage = (serverIncoming.receive() as Frame.Binary).buffer.toString()
////                val id = getAttributeFromActionJson(serverProvideIdMessage,"id") as String
//                println("1 <-- $serverProvideIdMessage")
////    println("Server provides id $id [$serverProvideIdMessage]")
////                assertNotNull(id)
//            }
//        }
//    }


    @Test
    fun testServerProvidesIdIfEmpty() {
        withTestApplication(Application::main) {
            handleWebSocketConversation("/") { serverIncoming, clientOutgoing ->
//                val id = step1GetIDFromServer(serverIncoming, clientOutgoing)
//                assertNotNull(id)

                val serverProvideIdMessage = (serverIncoming.receive() as Frame.Binary).buffer.toString()
//                val id = getAttributeFromActionJson(serverProvideIdMessage,"id") as String
                println("1 <-- $serverProvideIdMessage")
//    println("Server provides id $id [$serverProvideIdMessage]")
//                assertNotNull(id)
            }
        }
    }

//    @Test
//    fun testServerDoesNotProvideIdIfSpecified() {
//        withTestApplication(Application::main) {
//            val id = "1234"
//            handleWebSocketConversation("/?colyseusid=$id") { serverIncoming, clientOutgoing ->
//                //                val id = step1GetIDFromServer(serverIncoming, clientOutgoing)
//                step2ClientRequestRoomToJoin(serverIncoming, clientOutgoing)
//                val roomId = step3ServerReturnRoomID(serverIncoming, clientOutgoing)
//                val endpoint = getEndpoint(id, roomId)
//                handleWebSocketConversation(endpoint) { roomIncoming, clientRoomOutgoing ->
//                    step4ClientConnectToRoom(roomIncoming, clientRoomOutgoing, roomId)
//                }
//            }
//        }
//    }
//    /**
//     * This is an integration test that verifies the behaviour of a simple conversation with an empty server.
//     */
//    @Test
//    fun testJoinRoomMessaging() {
//        // First we create a [TestApplicationEngine] that includes the module [Application.main],
//        // this executes that function and thus installs all the features and routes to this test application.
//        withTestApplication(Application::main) {
//            withTestApplication(Application::main) {
//                handleWebSocketConversation("/") { serverIncoming, clientOutgoing ->
//                    val id = step1GetIDFromServer(serverIncoming, clientOutgoing)
//                    step2ClientRequestRoomToJoin(serverIncoming, clientOutgoing)
//                    val roomId = step3ServerReturnRoomID(serverIncoming, clientOutgoing)
//                    val endpoint = getEndpoint(id, roomId)
//                    handleWebSocketConversation(endpoint) { roomIncoming, clientRoomOutgoing ->
//                        step4ClientConnectToRoom(roomIncoming, clientRoomOutgoing, roomId)
//                    }
//                }
//            }
//        }
//    }


}