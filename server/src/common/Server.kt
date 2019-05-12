package common


import com.example.SomeApplication
import com.example.common.*
import com.example.common.matchmaker.RegisteredHandler
import com.example.common.presence.LocalPresence
import com.example.common.presence.Presence
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.*
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.websocket.DefaultWebSocketServerSession
import org.slf4j.LoggerFactory
import kotlin.reflect.jvm.jvmName


/**
 * Class in charge of the logic of the chat server.
 * It contains handlers to events and commands to send messages to specific users in the server.
 */
class Sever {
//    /**
//     * Atomic counter used to get unique user-names based on the maxiumum users the server had.
//     */
//    val usersCounter = AtomicInteger()
//
//    /**
//     * A concurrent map associating session IDs to user names.
//     */
//    val memberNames = ConcurrentHashMap<String, String>()
//
    // code would look lik
    // val list = members.computeIfAbsent(member) { CopyOnWriteArrayList<WebSocketSession>() }
    // list.add(socket)
//    /**
//     * Associates a session-sessionId to a set of websockets.
//     * Since a browser is able to open several tabs and windows with the same cookies and thus the same session.
//     * There might be several opened sockets for the same client.
//     */
//    val members = ConcurrentHashMap<String, MutableList<WebSocketSession>>()

    val presence: Presence

    val matchMaker: MatchMaker

    val processId = generateId()

    val pingTimeout: Long

    private val logger by lazy { LoggerFactory.getLogger(Sever::class.jvmName) }


    init {
        // TODO allow other options for presence
        presence = LocalPresence()
        matchMaker = MatchMaker(presence, processId)
        pingTimeout = 1500
    }

    /**
     * Sends a [message] to a list of [this] [WebSocketSession].
     */
    suspend fun List<WebSocketSession>.send(frame: Frame) {
        forEach {
            try {
                it.send(frame.copy())
            } catch (t: Throwable) {
                try {
                    it.close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, ""))
                } catch (ignore: ClosedSendChannelException) {
                    // at some point it will get closed
                }
            }
        }
    }

    suspend fun onConnection(socket: DefaultWebSocketServerSession, incoming: ReceiveChannel<Frame>) {

        // TODO determine if this is necessary
        // First of all we get the session.
        val session = socket.call.sessions.get<SomeApplication.GameSession>()
        // We check that we actually have a session. We should always have one,
        // since we have defined an interceptor before to set one.
        if (session == null) {
            socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return
        }

        // set client id
        val colyseusID = socket.call.parameters["colyseusid"]
        val id = colyseusID ?: generateId()
        val auth = socket.call.parameters["auth"]
        val options = socket.call.parameters["options"]
        val pingCount = 0
        val client = Client(
            socket,
            id,
            session.id,
            null,
            auth,
            pingCount
        )

        // ensure client has it's "colyseusid"
        if (colyseusID == null) {
            socket.sendAction(UserId(id, pingCount))
        }

        logger.debug("onConnect $client")
        // TODO handle on 'error'
        // TODO handle on 'pong'


        val roomId = socket.call.parameters["roomId"]
        if (roomId != null) {
            logger.debug("Attempting to connect to $roomId")
            // TODO verify can connect to room if provided on initial connection

            this.matchMaker.connectToRoom(client, roomId)
        } else {
            logger.debug("Connecting with no room ID")
//            client
//            this.onMessageMatchmaking(client)
            // TODO handle onMatchmaking, unless it's handled for us in lower consumeEach
        }

        // We notify that a member joined by calling the server handler [memberJoin]
        // This allows to associate the session sessionId to a specific WebSocket connection.
//        this.memberJoin(session.id, socket)
        try {
            // We starts receiving messages (frames).
            // Since this is a coroutine. This coroutine is suspended until receiving frames.
            // Once the connection is closed, this consumeEach will finish and the code will continue.
            incoming.consumeEach { frame ->
                logger.debug("onMessage")

                // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                // We are only interested in textual messages, so we filter it.
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    logger.debug("Frame.Text: $message")
                    receiveMessageMatchMaking(socket, client, message)

                    // TODO look up clients in room, or room
                    client.onMessageListener.forEach { it(message) }
                    logger.debug("Client: $client")
                }
            }
        } finally {
            logger.debug("onClose")
            client.onCloseListener.forEach{ it(1) }
//            client.listener?.close(1)
            // Either if there was an error, of it the connection was closed gracefully.
            // We notify the server that the member left.
//            this.memberLeft(session.id, socket)
        }
    }

    private fun onMessageMatchmaking(client: Client) {
        // TODO if join request

    }

    /**
     * A chat session is identified by a unique nonce ID. This nonce comes from a secure random source.
     */
//    data class GameSession(val sessionId: String)

    /**
     * We received a message. Let's process it.
     */
    private suspend fun receiveMessageMatchMaking(socket: WebSocketSession, client: Client, message: String) {

        val action = parseActionJSON(message)
//        val type = getActionTypeFromJson(message)
        // We are going to handle commands (text starting with '/') and normal messages
        when (action?.type) {
            ActionType.JOIN_REQUEST -> onJoinRequest(socket, client, action as JoinRequest)
            ActionType.USER_ID -> this
            ActionType.ROOM_LIST -> this
        }
    }

    private suspend fun onJoinRequest(socket: WebSocketSession, client: Client, action: JoinRequest) {
        logger.debug("onJoinRequest: $action")
        val roomName = action.room
        val joinOptions = action.joinOptions
//        joinOptions.clientId = client.id

        if (!this.matchMaker.hasHandler(roomName) && !isValidId(roomName)) {
            socket.sendAction(JoinError("no available handler for $roomName"))
        } else {
            // TODO: confirm retry logic, colyseus tries 3x
            val joinRequest = this.matchMaker.onJoinRoomRequest(client, roomName, joinOptions)
            socket.sendAction(JoinResponse(joinOptions?.requestId, joinRequest.roomId, joinRequest.processId))
            // once done
//            send[Protocol.JOIN_REQUEST](client, joinOptions.requestId, response.roomId, response.processId);
            // or send error
        }
    }


    fun gracefullyShutdown() {
        this.matchMaker.gracefullyShutdown()
    }

    fun <T> register(roomName: String, factory: () -> T, options: Object? = null, listener: RoomListener? = null): RegisteredHandler<T> {
        return this.matchMaker.registerHandler(roomName, factory, options, listener)
    }
}


