package com.example.common

import com.example.common.matchmaker.RegisteredHandler
import com.example.common.presence.LocalPresence
import com.example.common.presence.Presence
import com.gofficer.codenames.redux.actions.ClientOptions
import common.MatchMakingListener
import common.Room
import common.RoomListener
import org.slf4j.LoggerFactory
import kotlin.reflect.jvm.jvmName

class MatchMaker(var presence: Presence?, var processId: String?) {

    private val logger by lazy { LoggerFactory.getLogger(MatchMaker::class.jvmName) }

    val localRooms: MutableMap<String, Room<*>> = mutableMapOf()

    var handlers: MutableMap<String, RegisteredHandler<*>> = mutableMapOf()

    var isGracefullyShuttingDown = false

    init {
        if (presence == null) {
            presence = LocalPresence()
        }
        logger.debug("Registering Matchmaker with ${presence} and process sessionId ${processId} ")
    }


    suspend fun connectToRoom(client: Client, roomId: String) {
        logger.debug("Client $client attempting to connect to $roomId")
        val room = this.localRooms[roomId] ?: throw Error("connectToRoom(), room doesn't exist. roomId: $roomId")

        client.sessionId = this.presence?.get("$room:${client.id}")

        // Clean up what we don't need
        val clientOptions = client.options?.copy(
            auth = null,
            requestId = null
        )
        client.options = null

        room._onJoin(client, clientOptions, client.auth)
    }

    fun gracefullyShutdown() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getRoomChannel(roomId: String): String {
        return "$$roomId"
    }

    fun <T> registerHandler(
        name: String,
        factory: () -> T,
        options: Object?,
        listener: RoomListener?
    ): RegisteredHandler<T> {
//    inline fun <reified T> registerHandler(name: String, options: Object?): RegisteredHandler {

        logger.debug("Registering room $name")
        val registeredHandler = RegisteredHandler(factory, options, listener)
//        val registeredHandler = RegisteredHandler("A", options)

        this.handlers[name] = registeredHandler

        this.cleanupStaleRooms(name)

        return registeredHandler
    }

    fun hasHandler(name: String): Boolean {
        return this.handlers[name] != null
    }

    fun cleanupStaleRooms(name: String) {
        // TODO
    }

    fun create(roomName: String, options: ClientOptions?): String? {
        logger.debug("create [Room: $roomName]")
        val registeredHandler = this.handlers[roomName]

        if (registeredHandler?.factory != null) {
//            val room = registeredHandler.factory?.let { registeredHandler.it() }
//            val room = registeredHandler.factory() as Room<*>

//            var entity = registeredHandler.factory() as Room<*>

//            val room = entity()

            val room = registeredHandler.factory() as Room<*>
            room.listener = registeredHandler?.listener
            room.roomId = generateId()
            room.roomName = roomName

//            if (room.onInit)
            room?.onInit(options, registeredHandler.options)
            // TODO
//            room.presence = this.presence

            logger.debug("Room name: $roomName")

            if (room.requestJoin(options, true) > 0) {
                logger.debug("Spawning $roomName (${room.roomId} on process TBD")

                // TODO: Add listeners

//                room.on('lock', this.lockRoom.bind(this, roomName, room));
//                room.on('unlock', this.unlockRoom.bind(this, roomName, room));
//                room.on('join', this.onClientJoinRoom.bind(tfhis, room));
//                room.on('leave', this.onClientLeaveRoom.bind(this, room));
//                room.once('dispose', this.disposeRoom.bind(this, roomName, room));
                room.matchMakingListener = object : MatchMakingListener {
                    override fun lock(roomName: String, room: Room<*>) {
                        clearRoomReferences(room)
                        handlers[room.roomName]?.listener?.lock(room)
                    }

                    override fun unlock(roomName: String, room: Room<*>) {
                        // TODO determin eif in middle of creating room references
                        TODO ("Determine of creating rooms")
                        handlers[room.roomName]?.listener?.unlock(room)
                    }

                    override fun join(room: Room<*>) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        // TODO where do i get client
//                        handlers[room.roomName]?.listener?.join(room, client)
                    }

                    override fun leave(room: Room<*>) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        // TODO where do i get client
//                        handlers[room.roomName]?.listener?.leave(room, client)
                    }

                    override fun dispose(room: Room<*>) {

                        val roomId = room.roomId
                        val roomName = room.roomName
                        logger.debug("Disposing $roomName ($roomId) on process $processId")


                        // emit disposal on registered session handler
                        handlers[roomName]?.listener?.dispose(room)

                        if (roomId != null && roomName != null) {

                            // remove from alive rooms
                            presence?.srem("a_${roomName}", roomId);

                            // remove concurrency key
                            presence?.del(getHandlerConcurrencyKey(roomName));

                            // remove from available rooms
                            clearRoomReferences(room);

                            // unsubscribe from remote connections
                            presence?.unsubscribe(getRoomChannel(roomId))

                            // remove actual room reference
                            localRooms.remove(roomId)
                        }
                    }
                }
            }

            // room always start unlocked
            this.createRoomReferences(room, true)

            room.listener?.create(room)
            return room.roomId
        }

        return null

    }

    private fun getHandlerConcurrencyKey(roomName: String): String {
        return "$roomName:c";
    }

    private fun clearRoomReferences(room: Room<*>) {
        TODO()
    }

    private fun createRoomReferences(room: Room<*>, init: Boolean): Boolean {
        val roomId = room.roomId ?: return false
        val roomName = room.roomName ?: return false
        localRooms[roomId] = room

        // add unlocked room reference
        this.presence?.sadd(roomName, roomId)

        if (init) {
            // add presence
            // add alive room reference (a=all)
            this.presence?.sadd("a_$roomName", roomId)

            this.presence?.subscribe(this.getRoomChannel(roomId)) { message ->
                logger.debug("Lambda message $message")
                // TODO lots more
//                const [method, requestId, args] = message;
//
//                const reply = (code, data) => {
//                this.presence.publish(`${room.roomId}:${requestId}`, [code, [this.processId, data]]);
//            };
//
//                // reply with property value
//                if (!args && typeof (room[method]) !== 'function') {
//                    return reply(IpcProtocol.SUCCESS, room[method]);
//                }
//
//                // reply with method result
//                let response: any;
//                try {
//                    response = room[method].apply(room, args);
//
//                } catch (e) {
//                    debugAndPrintError(e.stack || e);
//                    return reply(IpcProtocol.ERROR, e.message || e);
//                }
//
//                if (!(response instanceof Promise)) {
//                    return reply(IpcProtocol.SUCCESS, response);
//                }
//
//                response.
//                    then((result) => reply(IpcProtocol.SUCCESS, result)).
//                catch((e) => {
//                    // user might have called `reject()` without arguments.
//                    const err = e && e.message || e;
//                    reply(IpcProtocol.ERROR, err);
//                });
//            });
            }
        }
        return true
    }

    suspend fun onJoinRoomRequest(client: Client, roomToJoin: String, clientOptions: ClientOptions?): JoinByRoom {
        logger.debug("onJoinRoomRequest: $roomToJoin")
        val hasHandler = this.hasHandler(roomToJoin)

        var roomId: String? = null
        var processId: String? = null

        var roomToJoinOrRejoin: String = roomToJoin

        // TODO
        val isReconnect = clientOptions?.sessionId != null
        val sessionId = clientOptions?.sessionId ?: generateId()

        var isJoinById = !hasHandler && isValidId(roomToJoin)
        // rejoin requests come with session sessionId
        // TODO

        var shouldCreateRoom = hasHandler && !isReconnect

        if (isReconnect) {
            val reconnectRoom =
                this.presence?.get(sessionId) ?: throw MatchMakeError("rejoin has been expired for $sessionId")
            roomToJoinOrRejoin = reconnectRoom
        }

        if (isJoinById || isReconnect) {
            val sessionIdIfReconnect = if (isReconnect) sessionId else null
            val joinByID = this.joinById(roomToJoinOrRejoin, clientOptions, sessionIdIfReconnect)

            processId = joinByID.processId
            roomId = joinByID.roomId
        } else if (!hasHandler) {
            throw MatchMakeError("Failed to join invalid room $roomToJoinOrRejoin")
        }
        if (roomId == null && !isReconnect) {
            logger.debug("NO ROOM AND NOT A RECONNECT")

            // TODO await for available rooms
            // when multiple clients request to create a room simultaneously, we need
            // to wait for the first room to be created to prevent creating multiple of them

            // check if there's an existing room with provided name available to join
            val availableRoomsByScore = this.getAvailableRoomByScore(roomToJoin, clientOptions)

            // TODO finish implementing, for now return first for dummy logic
            if (availableRoomsByScore.isNotEmpty()) {
                logger.debug("Available rooms are not empty!")
                val joinByIdResponse = this.joinById(availableRoomsByScore.get(0).roomId, clientOptions)
                if (joinByIdResponse != null) {
                    logger.debug("Found room $roomId to join")
                    shouldCreateRoom = false
                    roomId = joinByIdResponse.roomId
                    processId = joinByIdResponse.processId
                }
            } else {
                logger.debug("No existing room to join")
            }

//            for (let i = 0, l = availableRoomsByScore.length; i < l; i++) {
//                // couldn't join this room, skip
//                const joinByIdResponse = (await this.joinById(availableRoomsByScore[i].roomId, clientOptions));
//                roomId = joinByIdResponse[1];
//
//                if (!roomId) { continue; }
//
//                const reserveSeatResponse = await this.remoteRoomCall(roomId, '_reserveSeat', [{
//                        id: client.id,
//                        sessionId,
//                }]);
//
//                if (reserveSeatResponse[1]) {
//                    // seat reservation was successful, no need to try other rooms.
//                    processId = reserveSeatResponse[0];
//                    shouldCreateRoom = false;
//                    break;
//
//                } else {
//                    processId = this.processId;
//                    shouldCreateRoom = true;
//                }
//            }


            val sessionIdIfReconnect = if (isReconnect) sessionId else null
            // join room by id
            val joinById = this.joinById(roomToJoinOrRejoin, clientOptions, sessionIdIfReconnect)
            // TODO: await room available

            // TODO: Get available rooms

            // TODO: Attempt to join
        }

        if (shouldCreateRoom) {
            roomId = this.create(roomToJoinOrRejoin, clientOptions)
        }

        if (roomId == null) {
            throw MatchMakeError("Failed to join invalid room $roomToJoinOrRejoin")
        } else if (shouldCreateRoom || isJoinById) {
            this.remoteRoomCall(roomId, "_reserveSeat", RemoteRoomArgs(client.id, sessionId))
        }

        return JoinByRoom(processId, roomId)
    }

    private fun getAvailableRoomByScore(roomName: String, clientOptions: ClientOptions?): List<RoomWithScore> {
        return this.getRoomsWithScore(roomName, clientOptions)
    }

    private fun getRoomsWithScore(roomName: String, clientOptions: ClientOptions?): List<RoomWithScore> {
        val roomsWithScore = mutableListOf<RoomWithScore>()
        val roomIds = this.presence?.smembers(roomName)
        val remoteRequestJoints = listOf<Any>()

        roomIds?.map {
            var maxClientsReached: Boolean
//            maxClientsReached = this.remoteRoomCall(roomId, "hasReachedMaxClients")

            var localRoom = this.localRooms[it]
            if (localRoom == null) {
                TODO("Remote not yet supported")
            } else {
                roomsWithScore.add(RoomWithScore(it, localRoom.requestJoin(clientOptions, false)))
            }
        }
        return roomsWithScore
    }


    private suspend fun joinById(
        roomId: String,
        clientOptions: ClientOptions?,
        rejoinSessionId: String? = null
    ): JoinByRoom {
        val exists = this.presence?.exists(this.getRoomChannel(roomId)) ?: false
        if (!exists) {
            logger.debug("Trying to join non-existant room '$roomId'")
        }

        if (rejoinSessionId != null) {
            val hasReservedSeatResponse = this.remoteRoomCall(roomId, "hasReservedSeat", listOf(rejoinSessionId));
            if (hasReservedSeatResponse.isSuccessful) {

            }
        }

        return JoinByRoom(null, roomId)
    }

    /**
     * TODO: implement properly
     */
    private fun remoteRoomCall(roomId: String, method: String, args: Any): RemoteRoomCall {
        // TODO: Multiple different people call with args.  Consider method overloading instead
        val room = this.localRooms[roomId]

        if (room != null) {
            val requestId = generateId()
            val channel = "$roomId:$requestId"

//            val unsubscribe = (String) ->
        }

        return RemoteRoomCall(null, true)
    }


}

data class JoinByRoom(val processId: String?, val roomId: String)
data class RemoteRoomCall(val processId: String?, val isSuccessful: Boolean)
data class RemoteRoomArgs(val id: String, val session: String)
data class RoomWithScore(val roomId: String, val score: Int)