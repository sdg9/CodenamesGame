package com.example.common

import io.ktor.http.cio.websocket.WebSocketSession

data class Client(
    var socket: WebSocketSession,
    var id: String,
    var sessionId: String?,
    var options: ClientOptions?,
    var auth: Any?,
    var pingCount: Int?
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