
import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.codenames.myServer.main
import com.squareup.moshi.Moshi
import io.ktor.application.*
import io.ktor.server.testing.*
import org.junit.Rule
import org.junit.rules.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.*

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

                    // Confirm state is updated accordingly

                }
            }
        }
    }
}