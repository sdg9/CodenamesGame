package com.example.common

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import java.nio.ByteBuffer

data class Client(
    var socket: WebSocketSession,
    var id: String,
    var sessionId: String?,
    var options: ClientOptions?,
    var auth: Any?,
    var pingCount: Int?,
    val useTextOverBinary: Boolean = false
//    var listener: ClientListner? = null
) {
    var onMessageListener = mutableListOf<(message: String) -> Unit>()
    var onCloseListener = mutableListOf<(code: Int) -> Unit>()

    fun setOnMessageListener(handler: (message: String) -> Unit ) {
        onMessageListener.add(handler)
    }
    fun setOnCloseListener(handler: (code: Int) -> Unit ) {
        onCloseListener.add(handler)
    }
    fun removeAllListeners() {
        onMessageListener = mutableListOf<(message: String) -> Unit>()
        onCloseListener = mutableListOf<(code: Int) -> Unit>()
    }
//
//    suspend fun send(action: Action) {
//        val json = toJSON(action)
//        if (useTextOverBinary) {
//            socket.send(json)
//        } else {
//            val data = json.toByteArray()
//            socket.send(Frame.Binary(true, ByteBuffer.wrap(data)))
//        }
//    }
//
//    suspend fun send(message: String) {
//        if (useTextOverBinary) {
//            socket.send(message)
//        } else {
//            val data = message.toByteArray()
//            socket.send(Frame.Binary(true, ByteBuffer.wrap(data)))
//        }
//    }
}

suspend inline fun <reified T> Client.send(input: T) {
    if (useTextOverBinary) {
        when (input) {
            is Action -> socket.send(toJSON(input))
            is String -> socket.send(input)
            else -> throw Error("Unable to understand message format for $input")
        }
    } else {
        when (input) {
            is Action -> socket.send(Frame.Binary(true, ByteBuffer.wrap(toJSON(input).toByteArray())))
            is String -> socket.send(Frame.Binary(true, ByteBuffer.wrap(input.toByteArray())))
            else -> throw Error("Unable to understand message format for $input")
        }
    }

}


data class ClientOptions(
    var auth: String?,
    var requestId: Int?,
    var sessionId: String?
)

//interface ClientListner {
//    fun message(message: String)
//    fun close(code: Int?)
//}