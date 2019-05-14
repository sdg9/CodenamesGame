package com.gofficer.colyseus.client

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.framing.CloseFrame;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.net.URI;
import java.net.URISyntaxException;
//import io.colyseus.fossil_delta.FossilDelta
//import io.colyseus.state_listener.StateContainer


class Room internal constructor(
    /**
     * Name of the room handler. Ex: "battle".
     */
    var name: String?, options: LinkedHashMap<String, Any>
) {

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

    val state: LinkedHashMap<String, Any>
        get() = state

    abstract class Listener () {

        /**
         * This event is triggered when the client leave the room.
         */
        open fun onLeave() {

        }

        /**
         * This event is triggered when some error occurs in the room handler.
         */
        open fun onError(e: Exception) {

        }

        /**
         * This event is triggered when the server sends a message directly to the client.
         */
        open fun onMessage(message: Any) {}

        /**
         * This event is triggered when the client successfuly joins the room.
         */
        open fun onJoin() {

        }

        /**
         * This event is triggered when the server updates its state.
         */
        open fun onStateChange(state: java.util.LinkedHashMap<String, Any>) {

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
            override fun onError(e: Exception) {
                //System.err.println("Possible causes: room's onAuth() failed or maxClients has been reached.");
                for (listener in listeners) {
//                    listener?.onError(e)
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                if (code == CloseFrame.PROTOCOL_ERROR && reason != null && reason.startsWith("Invalid status code received: 401")) {
                    for (listener in listeners) {
//                        listener?.onError(Exception(reason))
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


    private fun onMessageCallback(bytes: ByteArray) {
        //        System.out.println("Room.onMessageCallback()");
        try {
            val message = msgpackMapper.readValue<Any>(bytes, object : TypeReference<Any>() {

            })
            if (message is List<*>) {
                val messageArray = message as List<Any>
                if (messageArray.get(0) is Int) {
                    val code = messageArray.get(0) as Int
                    when (code) {
                        Protocol.JOIN_ROOM -> {
                            sessionId = messageArray.get(1) as String
                            for (listener in listeners) {
                                listener?.onJoin()
                            }
                        }

                        Protocol.JOIN_ERROR -> {
                            System.err.println("Error: " + messageArray.get(1))
                            for (listener in listeners) {
                                listener?.onError(Exception(messageArray.get(1).toString()))
                            }
                        }

                        Protocol.ROOM_STATE -> {
                            //                    const remoteCurrentTime = message[2];
                            //                    const remoteElapsedTime = message[3];
//                            setState(messageArray.get(1) as ByteArray)
                            println("Room State")

                            for (listener in listeners) {
//                                listener?.onMessage()
//                                println("Respons: ${messageArray.get(1)}")
                                // TODO pick up here
                                // determine how to best handle restoring message pack back to object
                                // Ideally work directly with object and not maps
                                val result = messageArray.get(1)
                                when (result) {
                                    String -> println("Result is string")
                                    else -> {
                                        print("Result is not string $result")
                                        print(result::class.java)
                                        print(result::class.java.simpleName)
                                    }
                                }
//                                listener?.onStateChange(messageArray.get(1) as String)
                            }
                        }

                        Protocol.ROOM_STATE_PATCH -> {
//                            patch(messageArray.get(1) as ArrayList<Int>)
                            println("Room state patch")
                        }

                        Protocol.ROOM_DATA -> {
                            for (listener in listeners) {
                                listener?.onMessage(messageArray.get(1))
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
                listener?.onError(e)
            }
        }

    }

    private fun dispatchOnMessage(message: Any) {
        for (listener in listeners) {
            listener?.onMessage(message)
        }
    }

    /**
     * Remove all event and data listeners.
     */
    fun removeAllListeners() {
//        super.removeAllListeners()
        this.listeners.clear()
    }

    /**
     * Disconnect from the room.
     */
    fun leave() {
        if (this.connection != null) {
            this.connection!!.send(Protocol.LEAVE_ROOM)
        } else {
            for (listener in listeners) {
                listener?.onLeave()
            }
        }
    }

    /**
     * Send message to the room handler.
     */
    fun send(data: Any) {
        val id = this.id
        if (this.connection != null && id != null)
            this.connection!!.send(Protocol.ROOM_DATA, id, data)
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

//    @Throws(IOException::class)
//    private fun setState(encodedState: ByteArray) {
//        this.set(msgpackMapper.readValue(encodedState, Any::class.java) as LinkedHashMap<String, Any>)
//        this._previousState = encodedState
//        for (listener in listeners) {
//            listener?.onStateChange(this.state)
//        }
//    }

//    @Throws(Exception::class)
//    private fun patch(binaryPatch: ArrayList<Int>) {
//        val baos = ByteArrayOutputStream()
//        for (i in binaryPatch) {
//            baos.write(i and 0xFF)
//        }
//        this._previousState = FossilDelta.apply(this._previousState!!, baos.toByteArray())
//        this.set(msgpackMapper.readValue(this._previousState, Any::class.java) as LinkedHashMap<String, Any>)
//        for (listener in listeners) {
//            listener?.onStateChange(this.state)
//        }
//    }
}