package com.gofficer.codenames.myServer

import com.gofficer.codenames.redux.actions.*
import com.gofficer.colyseus.server.Client
import com.gofficer.codenames.redux.createCodeNamesStore
import com.gofficer.codenames.redux.middleware.loggingMiddleware
import com.gofficer.codenames.redux.middleware.setupGameMiddleware
import com.gofficer.codenames.redux.middleware.validActionMiddleware
import com.gofficer.codenames.redux.reducers.reduceGameSetup
import com.gofficer.codenames.redux.utils.actionToNetworkBytes
import com.gofficer.codenames.redux.utils.protocolToAction
import com.gofficer.colyseus.network.*
import common.Room
import common.RoomListener
import gofficer.codenames.redux.game.GameState
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import redux.api.Dispatcher
import redux.api.Store
import redux.api.enhancer.Middleware
import java.nio.ByteBuffer
import kotlin.reflect.jvm.jvmName

private val logger by lazy { LoggerFactory.getLogger(MyRoom::class.jvmName) }
class MyRoom : Room<GameState>(listener = object : RoomListener {
    override fun create(room: Room<*>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dispose(room: Room<*>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun join(room: Room<*>, client: Client) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun leave(room: Room<*>, client: Client) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}) {
    private val initState: GameState = GameState()
    override lateinit var store: Store<GameState>

    init {
        createReduxStore()
    }

    private fun createReduxStore() {
        store = createCodeNamesStore(initState,
            arrayOf(
                reduceGameSetup
            ),
            arrayOf(
                loggingMiddleware,
                validActionMiddleware,
                setupGameMiddleware{ false },
                messageAllClientsMiddleware(clients)
            )
        )
        logger.debug("Dispatching setup game")
        store.dispatch(SetupGame())
    }

    override suspend fun onMessage(client: Client, protocolMessage: ProtocolMessage) {
        logger.debug("My custom implementation received $protocolMessage")
        val subProtocol = protocolMessage?.subProtocol
        val message = protocolMessage?.message

        if(subProtocol == null || message == null) {
            logger.warn("ProtocolMessage fields are null $protocolMessage")
            return
        }

        val deserializedAction = protocolToAction(protocolMessage)
        store.dispatch(deserializedAction)
//        when (deserializedAction) {
//            is TouchCard -> {
//                deserializedAction.isFromServer = true
//                sendAllClients(deserializedAction)
//            }
////            is ResetGame -> {
//////                deserializedAction.isFromServer = true
//////                store.dispatch(deserializedAction)
//////                sendAllClients(deserializedAction)
////            }
//            else -> {
//                logger.debug("Unidentified action for subprotocol $subProtocol")
//            }
//        }
    }

    private suspend fun sendAllClients(protocolMessage: ProtocolMessage) {
        logger.debug("Total clients: ${clients.size}")
        clients.forEach {
            it.socket.sendProtocolMessage(protocolMessage)
        }
    }


    private suspend fun sendAllClients(action: NetworkAction?) {
        logger.debug("Total clients: ${clients.size}")
        if (action == null) {
            return
        }
        clients.forEach {
            it.socket.sendAction(action)
        }
    }

    override fun onJoin(client: Client, options: Any?, auth: Any?) {
        logger.debug("Client ${client.id} joined room $roomId")
    }

    override fun onLeave(client: Client, consented: Boolean?) {
        logger.debug("Client ${client.id} left room $roomId")
    }
}


suspend inline fun WebSocketSession.sendProtocolMessage(protocolMessage: ProtocolMessage) {
    // TODO fix me.
//    val byteArray = pack(protocolMessage)
    val byteArray = protocolMessage.originalMessage
    val clientMessage = Frame.Binary(true, ByteBuffer.wrap(byteArray))
    send(clientMessage)
}



suspend inline fun WebSocketSession.sendBytes(byteArray: ByteArray) {
    val clientMessage = Frame.Binary(true, ByteBuffer.wrap(byteArray))
    send(clientMessage)
}


suspend inline fun WebSocketSession.sendAction(action: NetworkAction?) {
    if (action == null) {
        println("Action is null")
        return
    }
    val bytes = actionToNetworkBytes(action)
    val clientMessage = Frame.Binary(true, ByteBuffer.wrap(bytes))
    send(clientMessage)
}


fun messageAllClientsMiddleware(clients: MutableList<Client>): Middleware<GameState> {
    // Confirmed the isClient check works!
    return  Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->

        println("Sending message to clients ${clients.size}")
        if (action is NetworkAction) {
            action.isFromServer = true
            clients.forEach {
                GlobalScope.launch {
                    it.socket.sendAction(action)
                }
            }
        }
        //        println("setupGameMiddleware: $action")
//        if (isClient()) {
//            println("Setting up game but only a client")
//            next.dispatch(action)
//            action
//        } else {
//            println("Setting up game no server")
//
//            val action = next.dispatch(action)
//            if (action is SetupGame || action is ResetGame) {
//                store.dispatch(SetupCards(getXUniqueCards(25)))
//            }
//            next.dispatch(action)
//            action
//        }
        next.dispatch(action)
    }
}
