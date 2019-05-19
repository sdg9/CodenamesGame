package com.gofficer.codenames.myServer

import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.colyseus.server.Client
import com.gofficer.colyseus.server.sendAction
import com.gofficer.codenames.redux.actions.SetupGame
import com.gofficer.codenames.redux.actions.TouchCard
import com.gofficer.codenames.redux.createCodeNamesStore
import com.gofficer.codenames.redux.middleware.loggingMiddleware
import com.gofficer.codenames.redux.middleware.setupGameMiddleware
import com.gofficer.codenames.redux.middleware.validActionMiddleware
import com.gofficer.codenames.redux.models.cardReduce
import com.gofficer.codenames.redux.reducers.reduceGameSetup
import com.gofficer.colyseus.network.*
import common.Room
import common.RoomListener
import gofficer.codenames.redux.game.GameState
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import org.apache.commons.codec.binary.Hex
import org.slf4j.LoggerFactory
import redux.api.Store
import java.nio.ByteBuffer
import kotlin.reflect.jvm.jvmName


//data class MyRoomGameState(
//    val someText: String,
//    val someNumber: Int,
//    val someBoolean: Boolean
//)

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
                reduceGameSetup,
                cardReduce
            ),
            arrayOf(
                loggingMiddleware,
                validActionMiddleware,
                setupGameMiddleware{ false }
//                getNetworkActionMiddleware(game),
//                getNavigationMiddleware(game)
            )
        )
        store.dispatch(SetupGame())
    }

    override suspend fun onMessage(client: Client, protocolMessage: ProtocolMessage) {
        logger.debug("My custom implementation received $protocolMessage")
//        val protocolMessage = unpackUnknown(action)
        val subProtocol = protocolMessage?.subProtocol
        val message = protocolMessage?.message

        if(subProtocol == null || message == null) {
            logger.warn("ProtocolMessage fields are null $protocolMessage")
            return
        }

        when (subProtocol) {
            SubProtocol.TOUCH_CARD -> {
                logger.debug("Got touch card")
                val pattern: Regex = "(\\S{2})".toRegex()
                println()
                println(Hex.encodeHexString(message).replace(pattern, "$1 "))
//                val unpacked = MoshiPack.unpack<TouchCard>(message)
//
//                logger.debug("Unpack touch card: $unpacked")
//                store.dispatch(unpacked)
                sendAllClients(protocolMessage)
            }
            else -> {
                logger.debug("Custom protocol $subProtocol")
            }
        }
//
//        // TODO for now send back to show it's working
//        val byteArray = pack(Protocol.ROOM_DATA, SubProtocol.TOUCH_CARD, TouchCard(18))
//        val someMessage = Frame.Binary(true, ByteBuffer.wrap(byteArray))
//        clients.forEach {
//////            it.send(action as String)
////            it.socket.send(someMessage)
//////            it.socket.send(pack(action))
//////            it.socket.sendAction(action)
//        }
    }

    private suspend fun sendAllClients(protocolMessage: ProtocolMessage) {
        logger.debug("Total clients: ${clients.size}")
        clients.forEach {
            it.socket.sendProtocolMessage(protocolMessage)
        }
    }

    override fun onJoin(client: Client, options: Any?, auth: Any?) {
        logger.debug("Client ${client.id} joined room $roomId")
    }

    override fun onLeave(client: Client, consented: Boolean?) {
        logger.debug("Client ${client.id} left room $roomId")
    }

//    override val state: MyRoomGameState = MyRoomGameState("test", 12, false)
}


//val syncWithClientMiddleware = { game: CodenamesGame ->
//    Middleware  { store: Store<GameState>, next: Dispatcher, action: Any ->
//        if (action is NetworkAction && !action.isFromServer && game.room != null) {
//            println("Dispatching remotely: $action")
//
////            val gsonBuilder = GsonBuilder()
////            gsonBuilder.registerTypeAdapter(action::class.java, MenuContentInterfaceAdapter())
////            var gson = gsonBuilder.create()
//
////            var gson = Gson()
////            var jsonString = gson.toJson(NetworkMessage(action::class.java.simpleName, action))
//
////            log.debug("Sending $jsonString")
////            room?.send(action.toJson())
//            game.room?.send(NetworkMessage(action::class.java.simpleName, action))
//        }
//        next.dispatch(action)
//    }
//}
//fun messageHelper(protocolMessage: ProtocolMessage?) : Type? {
//    val subProtocol = protocolMessage?.subProtocol
//    val message = protocolMessage?.message
//    if(subProtocol == null || message == null) {
//        return null
//    }
//
//    return when (subProtocol) {
//        TestSubType.ITEM_1 -> moshiPack.unpack<SomeType>(message)
//        TestSubType.ITEM_2 -> moshiPack.unpack<SomeOtherType>(message)
//        else -> null
//    }
//}


suspend inline fun WebSocketSession.sendProtocolMessage(protocolMessage: ProtocolMessage) {
    // TODO fix me.
//    val byteArray = pack(protocolMessage)
    val byteArray = protocolMessage.originalMessage
    val clientMessage = Frame.Binary(true, ByteBuffer.wrap(byteArray))
    send(clientMessage)
}