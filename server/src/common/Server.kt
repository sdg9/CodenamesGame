package common


import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.colyseus.server.*
import com.gofficer.colyseus.server.matchmaker.RegisteredHandler
import com.gofficer.colyseus.server.presence.LocalPresence
import com.gofficer.colyseus.server.presence.Presence
import com.gofficer.codenames.redux.actions.*
import com.gofficer.colyseus.network.Protocol
import com.gofficer.colyseus.network.unpackUnknown
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.channels.*
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.websocket.DefaultWebSocketServerSession
import org.slf4j.LoggerFactory
import java.lang.Error
import kotlin.reflect.jvm.jvmName



data class GameSession2(val id: String)

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

    val moshiPack = MoshiPack()

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
        logger.debug("onConnection")
        // TODO determine if this is necessary
        // First of all we get the session.
        val session = socket.call.sessions.get<GameSession2>()
        // We check that we actually have a session. We should always have one,
        // since we have defined an interceptor before to set one.
        if (session == null) {
            socket.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return
        }

        // set client id
        val colyseusID = socket.call.parameters["colyseusid"]
        val id = if (colyseusID.isNullOrBlank()) generateId() else colyseusID
        val auth = socket.call.parameters["auth"]
        val options = socket.call.parameters["options"]
        val useTextOverBinary = socket.call.parameters["useTextOverBinary"] == "true"
//        val useTextOverBinary = true
        logger.debug("UseTextOverBinary: $useTextOverBinary")
        val pingCount = 0
        val client = Client(
            socket,
            id,
            session.id,
            null,
            auth,
            pingCount,
            useTextOverBinary
        )

        // ensure client has it's "colyseusid"
        if (colyseusID.isNullOrBlank()) {
//            client.send(UserId(id, pingCount))
            logger.debug("Sending user id [$id] to client")
            client.sendUserId(id)
//            socket.sendAction(UserId(id, pingCount))
        } else {
            logger.debug("Client already has id [$id]")
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
//                if (frame is Frame.Text) {
//                    val message = frame.readText()
//                    logger.debug("Frame.Text: $message")
//                    receiveMessageMatchMakingText(client, message)
//
//                    // TODO look up clients in room, or room
//                    client.onMessageListener.forEach { it(message) }
//                    logger.debug("Client: $client")
//                }
                if (frame is Frame.Binary) {
                    logger.debug("Receiveing binary frame")

                    val bytes = frame.readBytes()
                    val protocolMessage = unpackUnknown(bytes)
//                    val unpacked: List<Any> = moshiPack.unpack(frame.readBytes())
//                    logger.debug("Received binary frame ${unpacked.toString()}")
//                    val firstByte = unpacked[0] as? Double ?: throw Error("Unknown binary message format")
                    if (protocolMessage == null || protocolMessage.protocol == null) {
                        throw Error("Unknown binary message format")
                    }
                    val type = protocolMessage.protocol
//                    val type = firstByte.toInt()

//                    val message = unpackProtocol()
                    when (type) {
//                        Protocol.JOIN_ROOM -> receiveMessageMatchMakingBinary(client, type, frame)
                        Protocol.JOIN_REQUEST, Protocol.JOIN_ROOM -> {
                            logger.debug("Join request/room")
                            val unpacked: List<Any> = moshiPack.unpack(frame.readBytes())
                            val roomName = unpacked[1] as String
                            onJoinRequest(client, roomName, null)
                        }
                        Protocol.ROOM_DATA -> {
                            logger.debug("Room data receieved ")
                            client.onMessageListener.forEach { it(protocolMessage) }
//                            onMes
                        }
                    }
//                    if (type == 10) {
//                        logger.debug("Mathes 10")
//                    } else {
//                        logger.debug("Doesnt match 10")
//                    }

//                    val unpacker = MessagePack.newDefaultUnpacker(frame.readBytes())
//                    logger.debug("Unpacked")
//                    while (unpacker.hasNext()) {
//                        val format = unpacker.nextFormat
//                        logger.debug("Format: $format")
//                        val msgPackValue = unpacker.unpackValue()
//                        logger.debug(msgPackValue.toString())
//                    }
//                    unpacker.close()
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

//    /**
//     * A chat session is identified by a unique nonce ID. This nonce comes from a secure random source.
//     */
////    data class GameSession2(val sessionId: String)
//
//    /**
//     * We received a message. Let's process it.
//     */
//    private suspend fun receiveMessageMatchMakingText(client: Client, message: String) {
//
//        val action = parseActionJSON(message)
////        val type = getActionTypeFromJson(message)
//        // We are going to handle commands (text starting with '/') and normal messages
//        when (action?.type) {
//            ActionType.JOIN_REQUEST -> onJoinRequestText(client, action as JoinRequest)
//            ActionType.USER_ID -> this
//            ActionType.ROOM_LIST -> this
//        }
//    }


    private suspend fun receiveMessageMatchMakingBinary(client: Client, type: Int, frame: Frame.Binary) {
        when (type) {
            Protocol.JOIN_REQUEST, Protocol.JOIN_ROOM -> {
                val unpacked: List<Any> = moshiPack.unpack(frame.readBytes())
                val roomName = unpacked[1] as String
                onJoinRequest(client, roomName, null)
            }
//            ActionType.USER_ID -> this
//            ActionType.ROOM_LIST -> this
        }
    }

    private suspend fun onJoinRequest(client: Client, roomName: String, joinOptions: ClientOptions?) {
        logger.debug("onJoinRequest (Binary): $roomName")
        if (!this.matchMaker.hasHandler(roomName) && !isValidId(roomName)) {
            // TODO make binary
            client.send(JoinError("no available handler for $roomName"))
//            client.socket.sendAction(JoinError("no available handler for $roomName"))
        } else {
            // TODO: confirm retry logic, colyseus tries 3x
            val joinRequest = this.matchMaker.onJoinRoomRequest(client, roomName, joinOptions)
            client.sendJoinRoom(joinRequest.roomId, joinRequest.processId)
        }
    }


    private suspend fun onJoinRequestText(client: Client, action: JoinRequest) {
        logger.debug("onJoinRequestText: $action")
        val roomName = action.room
        val joinOptions = action.joinOptions
//        joinOptions.clientId = client.id

        if (!this.matchMaker.hasHandler(roomName) && !isValidId(roomName)) {
            client.send(JoinError("no available handler for $roomName"))
//            client.socket.sendAction(JoinError("no available handler for $roomName"))
        } else {
            // TODO: confirm retry logic, colyseus tries 3x
            val joinRequest = this.matchMaker.onJoinRoomRequest(client, roomName, joinOptions)
//            client.send(JoinResponse(joinOptions?.requestId, joinRequest.roomId, joinRequest.processId))
            client.sendJoinRoom(joinRequest.roomId, joinRequest.processId)
//            client.socket.sendAction(JoinResponse(joinOptions?.requestId, joinRequest.roomId, joinRequest.processId))
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


