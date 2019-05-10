package com.gofficer.client

import io.colyseus.fossil_delta.FossilDelta
import org.java_websocket.framing.CloseFrame
import org.msgpack.jackson.dataformat.MessagePackFactory
import com.fasterxml.jackson.databind.ObjectMapper
import io.colyseus.state_listener.StateContainer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException


class Room internal constructor(
        /**
         * Name of the room handler. Ex: "battle".
         */
        var name: String?, options: LinkedHashMap<String, Any>) : StateContainer(LinkedHashMap()) {

    internal var options: LinkedHashMap<String, Any>? = null
        set

    /**
     * The unique identifier of the room.
     */
    internal var id: String? = null

    /**
     * Unique session identifier.
     */
    var sessionId: String? = null

    private val listeners = ArrayList<Listener>()
    private var connection: Connection? = null
    private var _previousState: ByteArray? = null
    private val msgpackMapper: ObjectMapper

//    val state: LinkedHashMap<String, Any>
//        get() = state

    abstract class Listener protected constructor() {

        /**
         * This event is triggered when the client leave the room.
         */
         fun onLeave() {

        }

        /**
         * This event is triggered when some error occurs in the room handler.
         */
        fun onError(e: Exception) {

        }

        /**
         * This event is triggered when the server sends a message directly to the client.
         */
        fun onMessage(message: Any) {

        }

        /**
         * This event is triggered when the client successfuly joins the room.
         */
         fun onJoin() {

        }

        /**
         * This event is triggered when the server updates its state.
         */
         fun onStateChange(state: LinkedHashMap<String, Any>) {

        }
    }

    init {
        this.options = options
        this.msgpackMapper = ObjectMapper(MessagePackFactory())
    }//        System.out.println("Room created: name: " + roomName + ", options: " + options);

    fun addListener(listener: Listener) {
        this.listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        this.listeners.remove(listener)
    }

    @Throws(URISyntaxException::class)
    internal fun connect(endpoint: String, httpHeaders: Map<String, String>, connectTimeout: Int) {
        //        System.out.println("Room is connecting to " + endpoint);
        this.connection = Connection(URI(endpoint), connectTimeout, httpHeaders, object : Connection.Listener {
            override fun onMessage(string: String) {
                this@Room.onMessageCallback(string)
            }

            override fun onError(e: Exception) {
                //System.err.println("Possible causes: room's onAuth() failed or maxClients has been reached.");
                for (listener in listeners) {
                    // TODO
//                    if (listener != null) listener!!.onError(e)
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                if (code == CloseFrame.PROTOCOL_ERROR && reason != null && reason.startsWith("Invalid status code received: 401")) {
                    for (listener in listeners) {
                        listener?.onError(Exception(reason))
                    }
                }
                for (listener in listeners) {
                    listener?.onLeave()
                }
                removeAllListeners()
            }

            override fun onOpen() {

            }

            override fun onMessage(bytes: ByteArray) {
                this@Room.onMessageCallback(bytes)
            }
        })
    }


    private fun onMessageCallback(string: String) {
        // TODO
        println("TODO room message callback")
    }
    private fun onMessageCallback(bytes: ByteArray) {
        //        System.out.println("Room.onMessageCallback()");
        /*try {
            val message = msgpackMapper.readValue(bytes, object : TypeReference<Any>() {

            })
            if (message is List<*>) {
                val messageArray = message as List<Any>
                if (messageArray[0] is Int) {
                    val code = messageArray[0] as Int
                    when (code) {
                        Protocol.JOIN_ROOM -> {
                            sessionId = messageArray[1] as String
                            for (listener in listeners) {
                                if (listener != null) listener!!.onJoin()
                            }
                        }

                        Protocol.JOIN_ERROR -> {
                            System.err.println("Error: " + messageArray[1])
                            for (listener in listeners) {
                                if (listener != null) listener!!.onError(Exception(messageArray[1].toString()))
                            }
                        }

                        Protocol.ROOM_STATE -> {
                            //                    const remoteCurrentTime = message[2];
                            //                    const remoteElapsedTime = message[3];
                            setState(messageArray[1] as ByteArray)
                        }

                        Protocol.ROOM_STATE_PATCH -> {
                            patch(messageArray[1] as ArrayList<Int>)
                        }

                        Protocol.ROOM_DATA -> {
                            for (listener in listeners) {
                                if (listener != null) listener!!.onMessage(messageArray[1])
                            }
                        }

                        Protocol.LEAVE_ROOM -> {
                            leave()
                        }

                        else -> dispatchOnMessage(message)
                    }
                } else
                    dispatchOnMessage(message)
            } else
                dispatchOnMessage(message)
        } catch (e: Exception) {
            for (listener in listeners) {
                if (listener != null) listener!!.onError(e)
            }
        }
        */

    }

    private fun dispatchOnMessage(message: Any) {
        for (listener in listeners) {
            if (listener != null) listener!!.onMessage(message)
        }
    }

    /**
     * Remove all event and data listeners.
     */
    override fun removeAllListeners() {
        super.removeAllListeners()
        this.listeners.clear()
    }

    /**
     * Disconnect from the room.
     */
    fun leave() {
        if (this.connection != null) {
            this.connection?.send(Protocol.LEAVE_ROOM)
        } else {
            for (listener in listeners) {
                if (listener != null) listener!!.onLeave()
            }
        }
    }

    /**
     * Send message to the room handler.
     */
    fun send(data: Any) {
        if (this.connection != null)
            this.connection?.send(Protocol.ROOM_DATA, this.id, data)
        else {
            // room is created but not joined yet
            for (listener in listeners) {
                listener?.onError(Exception("send error: Room is created but not joined yet"))
            }
        }
    }


    fun hasJoined(): Boolean {
        return this.sessionId != null
    }

    @Throws(IOException::class)
    private fun setState(encodedState: ByteArray) {
        this.set(msgpackMapper.readValue(encodedState, Any::class.java) as LinkedHashMap<String, Any>)
        this._previousState = encodedState
        for (listener in listeners) {
            if (listener != null) listener!!.onStateChange(this.state)
        }
    }

    @Throws(Exception::class)
    private fun patch(binaryPatch: ArrayList<Int>) {
        val baos = ByteArrayOutputStream()
        for (i in binaryPatch) {
            baos.write(i and 0xFF)
        }
        this._previousState = FossilDelta.apply(this._previousState!!, baos.toByteArray())
        this.set(msgpackMapper.readValue(this._previousState, Any::class.java) as LinkedHashMap<String, Any>)
        for (listener in listeners) {
            if (listener != null) listener!!.onStateChange(this.state)
        }
    }
}