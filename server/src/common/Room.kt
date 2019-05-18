package common

import com.gofficer.colyseus.server.*
import com.gofficer.colyseus.server.Protocol.Companion.WS_CLOSE_CONSENTED
import com.gofficer.colyseus.server.presence.Presence
import com.gofficer.codenames.redux.actions.ClientOptions
import com.gofficer.codenames.redux.actions.getActionTypeFromJson
import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import redux.api.Store
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule
import kotlin.reflect.jvm.jvmName


val DEFAULT_PATCH_RATE = 1000 / 20 // 20fps (50ms)
val DEFAULT_SEAT_RESERVATION_TIME = 5

sealed class RoomEvent {
    companion object : Event<RoomEvent>()

    class Lock() : RoomEvent() {
        fun emit() = Companion.emit(this)
    }


    class Unlock() : RoomEvent() {
        fun emit() = Companion.emit(this)
    }


    class Join() : RoomEvent() {
        fun emit() = Companion.emit(this)
    }
}

interface RoomListener {
    fun create(room: Room<*>)
    fun dispose(room: Room<*>)
    fun join(room: Room<*>, client: Client)
    fun leave(room: Room<*>, client: Client)
    fun lock(room: Room<*>) {}
    fun unlock(room: Room<*>) {

    }
}

interface MatchMakingListener {
    fun lock(roomName: String, room: Room<*>)
    fun unlock(roomName: String, room: Room<*>)
    fun join(room: Room<*>)
    fun leave(room: Room<*>)
    fun dispose(room: Room<*>)
}

abstract class Room<T>(var presence: Presence? = null, var listener: RoomListener? = null, var matchMakingListener: MatchMakingListener? = null) {

    private val logger by lazy { LoggerFactory.getLogger(Room::class.jvmName) }

    var roomId: String? = null
    var roomName: String? = null

    private var maxClients: Int = Int.MAX_VALUE
    var patchRate =DEFAULT_PATCH_RATE
    var autoDispose = true

    abstract val store: Store<T>
//    abstract val state: T
    var metaData: Any? = null

    val clients: MutableList<Client> = mutableListOf()

    var seatReservationTime = DEFAULT_SEAT_RESERVATION_TIME
    val reservedSeats: MutableSet<String> = mutableSetOf()
    val reservedSeatTimeouts: MutableMap<String, TimerTask> = mutableMapOf()

    private val reconnections = ConcurrentHashMap<String, String>()
    var isDisconnecting = false

    // TODO serializer
    // TODO afterNextPathcBroadcasts

    // TODO simulationINterval
    // TODO patchInterval

    private var isLocked = false
    private var isLockedExplicitly = false
    private var maxClientsReached = false

    // TODO autoDisposeTimeout


    val thisRoom = this

    init {
        RoomEvent on {
            when (it) {
                is RoomEvent.Join -> println("Join event detected")
            }
        }


    }

    // Abstract methods
    abstract suspend fun onMessage(client: Client, data: Any): Unit

    // Optional interfaces
    open fun onInit(clientOptions: ClientOptions?, handlerOptions: Object?): Unit {}
    open fun onJoin(client: Client, options: Any?, auth: Any?) {}
    open fun onLeave(client: Client, consented: Boolean?) {}
    open fun onDispose() {}

//    /**
//     * An interface for listening to client events
//     */
//    abstract class Listener {
//
//        fun onInit(options: Any) {}
//
//        fun onJoin(client: Client, options: ClientOptions?, auth: Any?) {}
//
//        /**
//         * This event is triggered when the connection is accepted by the server.
//         *
//         * @param id ColyseusId provided by the server
//         */
//        abstract fun onConnect(id: String)
//
//
//        /**
//         * This event is triggered when an unhandled message comes to client from server
//         *
//         * @param message The message from server
//         */
//        abstract fun onMessage(sessionId: String, message: Any)
//
//        /**
//         * This event is triggered when the connection is closed.
//         *
//         * @param code   The codes can be looked up here: [org.java_websocket.framing.CloseFrame]
//         * @param reason Additional information
//         * @param remote Whether or not the closing of the connection was initiated by the remote host
//         */
//        abstract fun onClose()
//    }

//    suspend fun onConnection(ws: DefaultWebSocketServerSession, incoming: ReceiveChannel<Frame>) {
//
//        // First of all we get the session.
//        val session = ws.call.sessions.get<SomeApplication.GameSession2>()
//
//        // We check that we actually have a session. We should always have one,
//        // since we have defined an interceptor before to set one.
//        if (session == null) {
//            ws.close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
//            return
//        }
//
//        logger.debug("onConnect ${session.id}")
//        listener?.onConnect(session.id)
//
//        // We notify that a member joined by calling the server handler [memberJoin]
//        // This allows ti associate the session sessionId to a specific WebSocket connection.
////        this.memberJoin(session.sessionId, ws)
//
//        try {
//            // We starts receiving messages (frames).
//            // Since this is a coroutine. This coroutine is suspended until receiving frames.
//            // Once the connection is closed, this consumeEach will finish and the code will continue.
//            incoming.consumeEach { frame ->
//                logger.debug("onMessage")
//                // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
//                // We are only interested in textual messages, so we filter it.
//                if (frame is Frame.Text) {
//                    // Now it is time to process the text sent from the user.
//                    // At this point we have context about this connection, the session, the text and the server.
//                    // So we have everything we need.
////                    receivedMessage(session.sessionId, frame.readText())
//                    listener?.onMessage(session.id, frame.readText())
//                }
//            }
//        } finally {
//            logger.debug("onClose")
//            listener?.onClose()
//            // Either if there was an error, of it the connection was closed gracefully.
//            // We notify the server that the member left.
////            this.memberLeft(session.sessionId, ws)
//        }
//    }

    fun requestJoin(options: Any?, isNew: Boolean?): Int {
        return 1
    }

    suspend fun _onJoin(client: Client, options: ClientOptions?, auth: Any?): Unit? {
        logger.debug("_onJoin room $this")

        // TODO: Is this working as intended?
        client.setOnMessageListener { message ->
            logger.debug("Message: ${client.id}")
            //TODO is this correct?
            GlobalScope.launch {
                _onMessage(client, message)
            }
        }
        client.setOnCloseListener { code ->
            logger.debug("Close: ${client.id}")
            _onLeave(client, code)
        }
//        client.listener = object : ClientListner  {
//            override fun message(message: String) {
//                logger.debug("Message: ${client.id}")
//                _onMessage(client, message)
//            }
//
//            override fun close(code: Int?) {
//                logger.debug("Close: ${client.id}")
//                _onLeave(client, code)
//            }
//        }

        this.clients.add(client)
        this.reservedSeats.remove(client.sessionId)

        // delete seat reservation
        this.reservedSeatTimeouts[client.sessionId]?.cancel()
        this.reservedSeatTimeouts.remove(client.sessionId)

        // TODO clear auto-dispose timeout

        // lock when maxClients reached
        if (!this.isLocked && this.clients.size >= this.maxClients) {
            this.maxClientsReached = true
            this.lock()
        }

        // TODO confirm room id maches the room requested to join
        val roomId = this.roomId
        if (roomId != null) {
            // TODO: Coroutine - Confirm if this is how I should do it
//            GlobalScope.launch {
              client.sendJoinConfirmation(roomId)
//                client.send(JoinResponseConfirmation(roomId))
//                client.socket.sendAction(JoinResponseConfirmation(roomId))
//            }
        }

        // send current state when new client joins the room
        if (this.store.state != null) {
            logger.debug("State isn't null, sending to client")
            this.sendState(client)
        } else {
            logger.debug("State is null, nothing to send client")
        }

        val reconnection = client.sessionId?.let { reconnections?.get(it) }
        println("Reconnection: $reconnection")
        if (reconnection != null) {
            TODO("Implement reconnection")
//            reconnection.resolve(client)
        } else {
            return this?.onJoin(client, options, auth)
        }
    }

    private suspend fun sendState(client: Client) {
        logger.debug("State: ${store.state}")
        client.sendRoomState(this.store.state)
    }

    private suspend fun _onMessage(client: Client, message: String) {
        logger.debug("Message $message from $client")

        if (message == null) {
            // Or in future if trouble decoding
            logger.debug("$roomName ($roomId), couldn't decode message: $message")
            return
        }

        // TODO temporary conditionals until implemented
        val isRoomData = false
        val isLeaveRoom = false

        if (isRoomData) {
            TODO("isRoomData")
            // this.onMessage(client, message[2]);
        } else if (isLeaveRoom) {
            client.removeAllListeners()
            // only effectively close connection when "onLeave" is fulfilled
            this._onLeave(client, WS_CLOSE_CONSENTED)
            // TODO delay until after onLeave is fired
            client.socket.close()
        } else {
            // TODO received string, convert to action:
            val type = getActionTypeFromJson(message)
            if (type != null) {
                onMessage(client, message)
            } else {
                TODO ("Deal with non-Action style message")
            }
        }

    }

    fun allowReconnection(client: Client, seconds: Long) {
        if (isDisconnecting) {
            throw Error("Disconnecting")
        }
        this._reserveSeat(client, seconds, true)

        // keep reconnection reference in case the user reconnects into the room
//        val reconnection = Deferred()

        // expire seat reservation after timeout
        // TODO
//        val timer = Timer().schedule(seconds * 1000){
//            reconnection.reject(false)
//        }
//        this.reconnections.put(client.sessionId, timer)

        val cleanup = {
            reservedSeats.remove(client.sessionId)
            reconnections.remove(client.sessionId)
            reservedSeatTimeouts.remove(client.sessionId)
        }

        // TODO figure out how to translate javascript Promise/Deferred to Kotlin
//        reconnection

    }

    private fun _reserveSeat(client: Client, seconds: Long, allowReconnection: Boolean): Boolean {
        if (!allowReconnection && hasReachedMaxClients()) {
            return false
        }

        val sessionId = client.sessionId
        val myRoomId = roomId
        if (sessionId != null && myRoomId != null) {
            reservedSeats.add(sessionId)
            presence?.setex(sessionId, myRoomId, seconds)
        }

        if (allowReconnection) {
            // TODO
//            this.reservedSeatTimeouts[client.sessionId] = setTimeout(() =>
//            this.reservedSeats.delete(client.sessionId), seconds * 1000);
            this.resetAutoDisposeTimeout(seconds);
        }
        return true
    }

    private fun resetAutoDisposeTimeout(seconds: Long) {
        // TODO
//        clearTimeout(this._autoDisposeTimeout);
//
//        if (!this.autoDispose) {
//            return;
//        }
//
//        this._autoDisposeTimeout = setTimeout(() => {
//            this._autoDisposeTimeout = undefined;
//            this._disposeIfEmpty();
//        }, timeoutInSeconds * 1000);
    }

    private fun hasReachedMaxClients(): Boolean {
        return clients.size + reservedSeats.size >= maxClients
    }

    private fun _onLeave(client: Client, code: Int?) {
        this.clients.remove(client)
        this?.onLeave(client, code == WS_CLOSE_CONSENTED)
    }

    private fun lock() {
        if (this.isLocked) {
            return
        }
//        this.emit("lock") // TODO
        this.isLocked = true
    }


    private fun unlock() {
        if (!this.isLocked) {
            return
        }
//        this.emit("unlock") // TODO
        this.isLocked = false
    }
}