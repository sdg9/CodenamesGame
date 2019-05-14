package com.example

import com.example.common.Client
import com.example.common.sendAction
import com.gofficer.codenames.redux.actions.SetupGame
import com.gofficer.codenames.redux.createCodeNamesStore
import com.gofficer.codenames.redux.middleware.loggingMiddleware
import com.gofficer.codenames.redux.middleware.setupGameMiddleware
import com.gofficer.codenames.redux.middleware.validActionMiddleware
import com.gofficer.codenames.redux.models.cardReduce
import com.gofficer.codenames.redux.reducers.reduceGameSetup
import common.Room
import common.RoomListener
import gofficer.codenames.redux.game.GameState
import org.slf4j.LoggerFactory
import redux.api.Store
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

    override suspend fun onMessage(client: Client, action: Any) {
        logger.debug("My custom implementation received $action")

        clients.forEach {
            // TODO address this casting, sloppy
//            it.send(action as String)
            it.socket.sendAction(action)
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
