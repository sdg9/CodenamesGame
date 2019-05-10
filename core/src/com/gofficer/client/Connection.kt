package com.gofficer.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import org.msgpack.jackson.dataformat.MessagePackFactory

import java.net.URI
import java.nio.ByteBuffer
import java.util.LinkedList

class Connection internal constructor(uri: URI, connectTimeout: Int, httpHeaders: Map<String, String>, private val listener: Listener?) : WebSocketClient(uri, Draft_6455(), httpHeaders, connectTimeout) {

//    private val _enqueuedCalls = LinkedList<Array<out Any>>()
//    private val msgpackMapper: ObjectMapper

    internal interface Listener {
        fun onError(e: Exception)

        fun onClose(code: Int, reason: String, remote: Boolean)

        fun onOpen()

        fun onMessage(bytes: ByteArray)

        fun onMessage(string: String)
    }

    init {
//        this.msgpackMapper = ObjectMapper(MessagePackFactory())
        connect()
    }

//    internal fun send(vararg data: Any) {
//        if (isOpen) {
//            try {
////                send(msgpackMapper.writeValueAsBytes(data))
//                // TODO oadd msgpack later
////                send(data)
//                this@Connection.send(data)
//            } catch (e: Exception) {
//                onError(e)
//            }
//
//        } else {
//            // WebSocket not connected.
//            // Enqueue data to be sent when readyState == OPEN
//            this._enqueuedCalls.push(data)
//        }
//    }

    fun send(vararg data: Any?) {

        //TODO implement me with message pack
        return super.send("Dummy data")
    }

    override fun onOpen(handshakedata: ServerHandshake) {
        this.listener?.onOpen()
//        if (this@Connection._enqueuedCalls.size > 0) {
//            for (objects in this@Connection._enqueuedCalls) {
//                this@Connection.send(*objects)
//            }
//
//            // clear enqueued calls.
//            this@Connection._enqueuedCalls.clear()
//        }
//        if (this@Connection.listener != null) this@Connection.listener.onOpen()
    }

    override fun onMessage(message: String) {
        println("onMessage string: $message")
        this.listener?.onMessage(message)
//        this@Connection.listener?.onMessage(message)
//        println("Message received: $message")
        //        System.out.println("Connection.onMessage(String message)");
        //        System.out.println(message);
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        this.listener?.onClose(code, reason, remote)
//        if (this@Connection.listener != null) this@Connection.listener.onClose(code, reason, remote)
    }

    override fun onError(ex: Exception) {
        this.listener?.onError(ex)
//        if (this@Connection.listener != null) this@Connection.listener.onError(ex)
    }

    override fun onMessage(buf: ByteBuffer?) {
        val bytes = ByteArray(buf!!.capacity())
        buf.get(bytes, 0, bytes.size)

        println("onMessage byes: $bytes")
        this.listener?.onMessage(bytes)
//        println("Bytes recevied $buf")
        //        System.out.println("Connection.onMessage(ByteBuffer bytes)");
//        if (this@Connection.listener != null) {
//            val bytes = ByteArray(buf!!.capacity())
//            buf.get(bytes, 0, bytes.size)
//            this@Connection.listener.onMessage(bytes)
//        }
    }
}
