package com.gofficer.client

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_6455
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer


class EmptyClient : WebSocketClient {

    constructor(serverUri: URI, draft: Draft) : super(serverUri, draft) {}

    constructor(serverURI: URI) : super(serverURI) {}

    constructor(serverURI: String) : super(URI(serverURI), Draft_6455()) {}

    override fun onOpen(handshakedata: ServerHandshake) {
        send("Hello, it is me. Mario :)")
        println("new connection opened")
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        println("closed with exit code $code additional info: $reason")
    }

    override fun onMessage(message: String) {
        println("received message: $message")
    }

    override fun onMessage(message: ByteBuffer) {
        println("received ByteBuffer")
    }

    override fun onError(ex: Exception) {
        System.err.println("an error occurred:$ex")
    }

}