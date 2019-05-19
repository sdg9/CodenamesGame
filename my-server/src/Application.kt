package com.gofficer.codenames.myServer

import com.gofficer.colyseus.server.Client
import common.GameSession
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
//            cookie<GameSession>("SESSION")
            header<GameSession>("Session")
        }

        // This adds an interceptor that will create a specific session in each request if no session is available already.
        intercept(ApplicationCallPipeline.Features) {
            if (call.sessions.get<GameSession>() == null) {
                call.sessions.set(GameSession(generateNonce()))
            }
        }

        /**
         * Now we are going to define routes to handle specific methods + URLs for this application.
         */
        routing {
            webSocket("/{roomId}") {
                gameServer.onConnection(this, incoming)
            }
            webSocket("/") { // this: WebSocketSession ->
//                val userId = call.parameters["colyseusid"]
                gameServer.onConnection(this, incoming)
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
}