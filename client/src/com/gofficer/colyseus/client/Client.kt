package com.gofficer.colyseus.client


import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.msgpack.core.MessagePack
import org.msgpack.jackson.dataformat.MessagePackFactory
import org.msgpack.value.Value
import org.msgpack.value.ValueType

import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.LinkedHashMap


class Client(
    private val hostname: String,
    id: String? = null,
    options: LinkedHashMap<String, Any>? = null,
    httpHeaders: LinkedHashMap<String, String>? = null,
    private val connectTimeout: Int = 0,
    private val listener: Listener? = null
) {

    /**
     * Unique identifier for the client.
     */
    var id: String? = null
        private set
    private val httpHeaders: LinkedHashMap<String, String>

    private var connection: Connection? = null
//    var rooms = LinkedHashMap<String, Room>()
    var rooms = mutableMapOf<String, Room>()
    var connectingRooms = LinkedHashMap<Int, Room>()
    private var requestId = 0
    private val availableRoomsRequests = LinkedHashMap<Int, AvailableRoomsRequestListener>()
    private val msgpackMapper: ObjectMapper
    private val defaultMapper: ObjectMapper

    /**
     * An interface for listening to client events
     */
    interface Listener {

        /**
         * This event is triggered when the connection is accepted by the server.
         *
         * @param id ColyseusId provided by the server
         */
        fun onOpen(id: String?)


        /**
         * This event is triggered when an unhandled message comes to client from server
         *
         * @param message The message from server
         */
        fun onMessage(message: Any)

        /**
         * This event is triggered when the connection is closed.
         *
         * @param code   The codes can be looked up here: [org.java_websocket.framing.CloseFrame]
         * @param reason Additional information
         * @param remote Whether or not the closing of the connection was initiated by the remote host
         */
        fun onClose(code: Int, reason: String, remote: Boolean)

        /**
         * This event is triggered when some error occurs in the server.
         */
        fun onError(e: Exception)
    }

    interface GetAvailableRoomsCallback {
        fun onCallback(availableRooms: ArrayList<AvailableRoom>?, error: String?)
    }

    interface AvailableRoomsRequestListener {
        fun callback(availableRooms: ArrayList<AvailableRoom>)
    }

    class AvailableRoom {

        var clients: Int = 0
        var maxClients: Int = 0
        var roomId: String? = null
        var metadata: Value? = null

        override fun toString(): String {
            return "{" +
                    "clients:" + clients + ", " +
                    "maxClients:" + maxClients + ", " +
                    "roomId:" + roomId + ", " +
                    "metadata:" + metadata + ", " +
                    "}"
        }
    }

    constructor(url: String, listener: Listener) : this(url, null, null, null, 0, listener) {}

    constructor(url: String, id: String, listener: Listener) : this(url, id, null, null, 0, listener) {}

    init {
        this.id = id
        this.httpHeaders = httpHeaders ?: LinkedHashMap()
        this.defaultMapper = ObjectMapper()
        this.msgpackMapper = ObjectMapper(MessagePackFactory())
        this.connect(options ?: LinkedHashMap(), connectTimeout)
    }

    /**
     * Joins room
     *
     * @param roomName can be either a room name or a roomId
     */
    fun join(roomName: String): Room {
        return this.createRoomRequest(roomName, null)
    }

    fun join(roomName: String, options: LinkedHashMap<String, Any>): Room {
        return this.createRoomRequest(roomName, options)
    }

    /**
     * Reconnects the client into a room he was previously connected with.
     */
    fun rejoin(roomName: String, sessionId: String): Room {
        val options = LinkedHashMap<String, Any>()
        options["sessionId"] = sessionId
        return this.join(roomName, options)
    }

    private fun createRoomRequest(roomName: String, options: LinkedHashMap<String, Any>?): Room {
        println("Creating room request")
        var options = options
        //        System.out.println("createRoomRequest(" + roomName + "," + options + "," + reuseRoomInstance + "," + retryTimes + "," + retryCount)
        if (options == null) options = LinkedHashMap()
        options["requestId"] = ++this.requestId

        val room = createRoom(roomName, options)

        val finalOptions = options
        room.addListener(object : Room.Listener() {
            public override fun onLeave() {
                rooms.remove(room.id)
                connectingRooms.remove(finalOptions["requestId"])
            }
        })

        this.connectingRooms[options["requestId"] as Int] = room

        this.connection!!.send(Protocol.JOIN_ROOM, roomName, options)

        return room
    }

    private fun createRoom(roomName: String, options: LinkedHashMap<String, Any>): Room {
        return Room(roomName, options)
    }

    /**
     * List all available rooms to connect with the provided roomName. Locked rooms won't be listed.
     */
    fun getAvailableRooms(roomName: String, callback: GetAvailableRoomsCallback) {
        // reject this promise after 10 seconds.

        ++this.requestId

        val requestIdFinal = this.requestId

        Thread(Runnable {
            try {
                Thread.sleep(10000)
                if (availableRoomsRequests.containsKey(requestIdFinal)) {
                    availableRoomsRequests.remove(requestIdFinal)
                    callback.onCallback(null, "timeout")
                }
            } catch (ignored: Exception) {

            }
        })

        // send the request to the server.
        this.connection!!.send(Protocol.ROOM_LIST, requestIdFinal, roomName)

        availableRoomsRequests[requestIdFinal] = object : AvailableRoomsRequestListener {
            override fun callback(availableRooms: ArrayList<AvailableRoom>) {
                availableRoomsRequests.remove(requestIdFinal)
                callback.onCallback(availableRooms, null)
            }
        }
    }

    /**
     * Close connection with the server.
     */
    fun close() {
        this.connection!!.close()
    }

    private fun connect(options: LinkedHashMap<String, Any>, connectTimeout: Int) {
        val uri: URI
        try {
            uri = URI(buildEndpoint("", options))
        } catch (e: URISyntaxException) {
            if (this@Client.listener != null)
                this@Client.listener.onError(e)
            return
        } catch (e: UnsupportedEncodingException) {
            if (this@Client.listener != null)
                this@Client.listener.onError(e)
            return
        } catch (e: JsonProcessingException) {
            if (this@Client.listener != null)
                this@Client.listener.onError(e)
            return
        }

        println("Calling connect $uri")
        this.connection = Connection(uri, connectTimeout, httpHeaders, object : Connection.Listener {
            override fun onError(e: Exception) {
                println("On error")
                if (this@Client.listener != null)
                    this@Client.listener.onError(e)
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                println("onClose")
                if (this@Client.listener != null)
                    this@Client.listener.onClose(code, reason, remote)
            }

            override fun onOpen() {
                println("Connection open!")
                println("ID: $id")
                if (this@Client.id != null && this@Client.listener != null) {
                    println("Calling onOpen listener")
                    this@Client.listener.onOpen(this@Client.id)
                }
            }

            override fun onMessage(bytes: ByteArray) {
                println("On Message")
                this@Client.onMessageCallback(bytes)
            }
        })
    }

    @Throws(UnsupportedEncodingException::class, JsonProcessingException::class)
    private fun buildEndpoint(path: String, options: LinkedHashMap<String, Any>?): String {
        // append colyseusid to connection string.
        var charset = "UTF-8"
        try {
            charset = StandardCharsets.UTF_8.name()
        } catch (ignored: NoClassDefFoundError) {
        }

        val sb = StringBuilder()
        if (options != null) {
            for (name in options.keys) {
                sb.append("&")
                sb.append(URLEncoder.encode(name, charset))
                sb.append("=")
                sb.append(URLEncoder.encode(defaultMapper.writeValueAsString(options[name]), charset))
            }
        }
        return this.hostname + "/" + path + "?colyseusid=" + URLEncoder.encode(
            if (this.id == null) "" else this.id,
            charset
        ) + sb.toString()
    }

    private fun onMessageCallback(bytes: ByteArray) {
        println("On message callback $bytes")
        //        System.out.println("Client.onMessageCallback()")
        try {
            val unpacker = MessagePack.newDefaultUnpacker(bytes)
            val `val` = unpacker.unpackValue()
            if (`val`.valueType == ValueType.ARRAY) {
                val arrayValue = `val`.asArrayValue()
                val codeValue = arrayValue.get(0)
                if (codeValue.valueType == ValueType.INTEGER) {
                    val code = codeValue.asIntegerValue().asInt()
                    when (code) {
                        Protocol.USER_ID -> {
                            //                            System.out.println("Protocol: USER_ID")
                            this.id = arrayValue.get(1).asStringValue().asString()
                            //                            System.out.println("colyseus id : " + this.id)
                            if (this@Client.listener != null) {
                                this@Client.listener.onOpen(this.id)
                            }
                        }
                        Protocol.JOIN_ROOM -> {
                            //                            System.out.println("Protocol: JOIN_ROOM")
                            val requestId = arrayValue.get(2).asIntegerValue().asInt()
                            //                            System.out.println("requestId: " + requestId)
                            val room = this.connectingRooms[requestId]
                            if (room == null) {
                                println("client left room before receiving session id.")
                                return
                            }
                            room.id = arrayValue.get(1).asStringValue().asString()
//                            room.setId(arrayValue.get(1).asStringValue().asString())
                            //                            System.out.println("room.id: " + room.getId())
                            val roomId = room.id
                            if (roomId != null) {
                                this.rooms[roomId] = room
                                room.connect(
                                    buildEndpoint(roomId, room.options),
                                    httpHeaders,
                                    this.connectTimeout
                                )
                            }
                            connectingRooms.remove(requestId)
                        }
                        Protocol.JOIN_ERROR -> {
                            //                            System.out.println("Protocol: JOIN_ERROR")
                            System.err.println("colyseus: server error: " + arrayValue.get(2).toString())
                            // general error
                            if (this.listener != null)
                                this.listener.onError(Exception(arrayValue.get(2).toString()))
                        }
                        Protocol.ROOM_LIST -> {
                            //                            System.out.println("Protocol: ROOM_LIST")
                            val id = arrayValue.get(1).asIntegerValue().asInt()
                            //                            System.out.println("id: " + id)
                            val roomsArrayValue = arrayValue.get(2).asArrayValue()
                            val availableRooms = ArrayList<AvailableRoom>()
                            for (i in 0 until roomsArrayValue.size()) {
                                val roomMapValue = roomsArrayValue.get(i).asMapValue()
                                val room = AvailableRoom()
                                for ((key, value) in roomMapValue.entrySet()) {
                                    when (key.asStringValue().asString()) {
                                        "clients" -> room.clients = value.asIntegerValue().asInt()
                                        "maxClients" -> room.maxClients = value.asIntegerValue().asInt()
                                        "roomId" -> room.roomId = value.asStringValue().asString()
                                        "metadata" -> room.metadata = value
                                    }
                                }
                                availableRooms.add(room)
                            }
                            if (this.availableRoomsRequests.containsKey(id)) {
                                this.availableRoomsRequests[id]?.callback(availableRooms)
                            } else {
                                println("receiving ROOM_LIST after timeout:$roomsArrayValue")
                            }
                        }
                        else -> {
                            // message is array, first element is integer but it is not a Protocol code
                            dispatchOnMessage(bytes)
                        }
                    }
                } else {
                    // message is array but first element is not integer
                    dispatchOnMessage(bytes)
                }
            } else {
                // message is not an array
                dispatchOnMessage(bytes)
            }
        } catch (e: Exception) {
            if (this.listener != null) this.listener.onError(e)
        }

    }

    @Throws(IOException::class)
    private fun dispatchOnMessage(bytes: ByteArray) {
        if (this@Client.listener != null)
            this@Client.listener.onMessage(msgpackMapper.readValue(bytes, object : TypeReference<Any>() {

            }))
    }
}