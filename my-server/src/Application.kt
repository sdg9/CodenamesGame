package com.gofficer.codenames.myServer

import com.gofficer.colyseus.server.Client
import common.GameSession2
import common.Room
import common.RoomListener
import common.Sever
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.content.defaultResource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import org.slf4j.LoggerFactory
import java.time.Duration
import kotlin.reflect.jvm.jvmName


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * Entry Point of the application. This function is referenced in the
 * resources/application.conf file inside the ktor.application.modules.
 *
 * Notice that the fqname of this function is io.ktor.samples.chat.ChatApplicationKt.main
 * For top level functions, the class name containing the method in the JVM is FileNameKt.
 *
 * The `Application.main` part is Kotlin idiomatic that specifies that the main method is
 * an extension of the [Application] class, and thus can be accessed like a normal member `myapplication.main()`.
 */
fun Application.main() {
    MyCodenamesServer().apply { main() }
}


private val logger by lazy { LoggerFactory.getLogger(MyCodenamesServer::class.jvmName) }

//fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * In this case we have a class holding our application state so it is not global and can be tested easier.
 */
class MyCodenamesServer {
    /**
     * This class handles the logic of a [Sever].
     * With the standard handlers [Sever.memberJoin] or [Sever.memberLeft] and operations like
     * sending messages to everyone or to specific people connected to the server.
     */
    private val gameServer = Sever()


    init {
        logger.debug("Init, about to register rooms")
        gameServer.register("public", ::MyRoom, null, object : RoomListener {
            override fun create(room: Room<*>) {
                logger.debug("Listener Room created: ${room.roomId}")
            }

            override fun dispose(room: Room<*>) {
                logger.debug("Listener Room disposed: ${room.roomId}")
            }

            override fun join(room: Room<*>, client: Client) {
                logger.debug("Listener Client ${client.id} joined room ${room.roomId}")
            }

            override fun leave(room: Room<*>, client: Client) {
                logger.debug("Listener Client ${client.id} left room ${room.roomId}")
            }

        })
        gameServer.register("otherRoom", ::MyRoom, null)
        gameServer.register("thirdRoom", ::MyRoom)
    }

    /**
     * This is the main method of the application in this class.
     */
    fun Application.main() {
        /**
         * First we install the features we need. They are bound to the whole application.
         * Since this method has an implicit [Application] receiver that supports the [install] method.
         */
        // This adds automatically Date and Server headers to each response, and would allow you to configure
        // additional headers served to each response.
        install(DefaultHeaders)
        // This uses use the logger to log every call (request/response)
        install(CallLogging)
        // This installs the websockets feature to be able to establish a bidirectional configuration
        // between the server and the client
        install(WebSockets) {
            // TODO temp remove ping
            pingPeriod = Duration.ofMinutes(1)
        }
        // This enables the use of sessions to keep information between requests/refreshes of the browser.
        install(Sessions) {
//            cookie<GameSession2>("SESSION")
            header<GameSession2>("Session")
        }

        // This adds an interceptor that will create a specific session in each request if no session is available already.
        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<GameSession2>() == null) {
                call.sessions.set(GameSession2(generateNonce()))
            }
        }

        /**
         * Now we are going to define routes to handle specific methods + URLs for this application.
         */
        routing {

            // This defines a websocket `/ws` route that allows a protocol upgrade to convert a HTTP request/response request
            // into a bidirectional packetized connection.
            webSocket("/{roomId}") {
//                log.debug("Attempting to connect to room ${call.parameters["sessionId"]}")
////                log.debug("User: ${call.parameters["userId"]}")
//                val roomId = call.parameters["roomId"]
//                val userId = call.parameters["userID"]
////                val myRoom = MyRoom()

//                myRoom.onConnection(this, incoming)
//                this.send("TODO rooms")
//                gameServer.matchMaker.handlers[roomId]?.factory
                gameServer.onConnection(this, incoming)
            }
            webSocket("/") { // this: WebSocketSession ->
                val userId = call.parameters["colyseusid"]
                gameServer.onConnection(this, incoming)

//                gameServer.register("room_name", MyRoom::javaClass)
//                // First of all we get the session.
//                val session = call.sessions.get<GameSession2>()
//
//                // We check that we actually have a session. We should always have one,
//                // since we have defined an interceptor before to set one.
//                if (session == null) {
//                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
//                    return@webSocket
//                }
//
//                // We notify that a member joined by calling the server handler [memberJoin]
//                // This allows to associate the session sessionId to a specific WebSocket connection.
//                server.memberJoin(session.sessionId, this)
//
//                try {
//                    // We starts receiving messages (frames).
//                    // Since this is a coroutine. This coroutine is suspended until receiving frames.
//                    // Once the connection is closed, this consumeEach will finish and the code will continue.
//                    incoming.consumeEach { frame ->
//                        // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
//                        // We are only interested in textual messages, so we filter it.
//                        if (frame is Frame.Text) {
//                            // Now it is time to process the text sent from the user.
//                            // At this point we have context about this connection, the session, the text and the server.
//                            // So we have everything we need.
//                            receivedMessage(session.sessionId, frame.readText())
//                        }
//                    }
//                } finally {
//                    // Either if there was an error, of it the connection was closed gracefully.
//                    // We notify the server that the member left.
//                    server.memberLeft(session.sessionId, this)
//                }
            }

            // This defines a block of static resources for the '/' path (since no path is specified and we start at '/')
            static {
                // This marks index.html from the 'web' folder in resources as the default file to serve.
                defaultResource("index.html", "web")
                // This serves files from the 'web' folder in the application resources.
                resources("web")
            }

        }
    }
//
//    /**
//     * A chat session is identified by a unique nonce ID. This nonce comes from a secure random source.
//     */
//    data class GameSession2(val id: String)
//
//    /**
//     * We received a message. Let's process it.
//     */
//    private suspend fun receivedMessage(sessionId: String, command: String) {
//        // We are going to handle commands (text starting with '/') and normal messages
//        when {
//            // The command `who` responds the user about all the member names connected to the user.
//            command.startsWith("/who") -> server.who(sessionId)
//            // The command `user` allows the user to set its name.
//            command.startsWith("/user") -> {
//                // We strip the command part to get the rest of the parameters.
//                // In this case the only parameter is the user's newName.
//                val newName = command.removePrefix("/user").trim()
//                // We verify that it is a valid name (in terms of length) to prevent abusing
//                when {
//                    newName.isEmpty() -> server.sendTo(sessionId, "server::help", "/user [newName]")
//                    newName.length > 50 -> server.sendTo(
//                        sessionId,
//                        "server::help",
//                        "new name is too long: 50 characters limit"
//                    )
//                    else -> server.memberRenamed(sessionId, newName)
//                }
//            }
//            // The command 'help' allows users to get a list of available commands.
//            command.startsWith("/help") -> server.help(sessionId)
//            // If no commands matched at this point, we notify about it.
//            command.startsWith("/") -> server.sendTo(
//                sessionId,
//                "server::help",
//                "Unknown command ${command.takeWhile { !it.isWhitespace() }}"
//            )
//            // Handle a normal message.
//            else -> server.message(sessionId, command)
//        }
//    }
}