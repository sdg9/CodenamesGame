package com.gofficer.colyseus.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import org.msgpack.jackson.dataformat.MessagePackFactory

import java.net.URI
import java.nio.ByteBuffer


class Connection internal constructor(
    uri: URI,
    connectTimeout: Int,
    httpHeaders: Map<String, String>,
    private val listener: Listener?
) : WebSocketClient(uri, Draft_6455(), httpHeaders, connectTimeout) {



    private val _enqueuedCalls = mutableListOf<Any>()
    private val msgpackMapper: ObjectMapper

    internal interface Listener {
        fun onError(e: Exception)

        fun onClose(code: Int, reason: String, remote: Boolean)

        fun onOpen()

        fun onMessage(bytes: ByteArray)
    }

    init {
        this.msgpackMapper = ObjectMapper(MessagePackFactory())
        connect()
    }//        System.out.println("Connection()");
    //        System.out.println("url is " + uri);

    internal fun send(vararg data: Any) {
        println("send: $data")
        if (isOpen()) {
            try {
                send(msgpackMapper.writeValueAsBytes(data))
            } catch (e: Exception) {
                onError(e)
            }

        } else {
            // WebSocket not connected.
            // Enqueue data to be sent when readyState == OPEN
            this._enqueuedCalls.add(data)
        }
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        println("onOpen: $handshakedata")
        if (this@Connection._enqueuedCalls.size > 0) {
            for (objects in this@Connection._enqueuedCalls) {
                this@Connection.send(objects)
            }

            // clear enqueued calls.
            this@Connection._enqueuedCalls.clear()
        }
        if (this@Connection.listener != null) this@Connection.listener.onOpen()
    }

    override fun onMessage(message: String) {
        println("onMessage: $message")
        //        System.out.println("Connection.onMessage(String message)");
        //        System.out.println(message);
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("onClose: $code")
        if (this@Connection.listener != null) this@Connection.listener.onClose(code, reason, remote)
    }

    override fun onError(ex: Exception) {
        println("onMessage: $ex")
        if (this@Connection.listener != null) this@Connection.listener.onError(ex)
    }

    override fun onMessage(buf: ByteBuffer) {
        println("onMessage: $buf")
        //        System.out.println("Connection.onMessage(ByteBuffer bytes)");
        if (this@Connection.listener != null) {
            val bytes = ByteArray(buf.capacity())
            buf.get(bytes, 0, bytes.size)
            this@Connection.listener.onMessage(bytes)
        }
    }
}