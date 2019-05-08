package com.gofficer.codenames

import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Logger
import com.gofficer.codenames.models.boardReduceSetup
import com.gofficer.codenames.models.cardReduce
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.screens.play.setupGameMiddleware
import com.gofficer.codenames.utils.logger
import com.gofficer.sampler.utils.toInternalFile
import gofficer.codenames.game.GameState
import gofficer.codenames.game.reduceGameSetup
import io.colyseus.Client
import redux.api.Dispatcher
import redux.api.Store
import redux.api.enhancer.Middleware
import redux.applyMiddleware
import redux.combineReducers
import redux.createStore
import io.colyseus.Room



class CodenamesGame : Game() {

    companion object {
        @JvmStatic
        private val log = logger<CodenamesGame>()
    }

    private var room: Room? = null
    private lateinit var client: Client
    val assetManager = AssetManager()
    lateinit var font24: BitmapFont
    private val initState: GameState = GameState()
    internal lateinit var store: Store<GameState>

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        assetManager.logger.level = Logger.DEBUG
        log.debug("create")

        initFonts()

        setScreen(LoadingScreen(this))

        store = createStore(
                combineReducers(
                        reduceGameSetup,
                        boardReduceSetup,
                        cardReduce
                ),
                initState,
                applyMiddleware(
                        loggingMiddleware,
                        validActionMiddleware,
                        networkActionMiddleware,
                        setupGameMiddleware
                ))

        client = Client("ws://localhost:2567", object : Client.Listener {
            override fun onOpen(id: String) {
                println("Client.onOpen();")
                println("colyseus id: $id")
            }

            override fun onMessage(message: Any) {
                println("Client.onMessage()")
                println(message)
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                println("Client.onClose();")
            }

            override fun onError(e: Exception) {
                println("Client.onError()")
                e.printStackTrace()
            }
        })

        room = client.join("my_room")
    }

    val networkActionMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
        if (action is NetworkAction) {
            println("Dispatching remotely: $action")
        }
        if (room != null) {
            log.debug("Sending $action")
        }
        room?.send(action.toString())
        next.dispatch(action)
    }

    override fun dispose() {
        super.dispose()

        log.debug("dispoase")
        assetManager.dispose()
        font24.dispose()
    }

    private fun initFonts() {
        val generator = FreeTypeFontGenerator("fonts/Arcon.ttf".toInternalFile())
        val params = FreeTypeFontGenerator.FreeTypeFontParameter()

        params.size = 24
        params.color = Color.BLACK
        font24 = generator.generateFont(params)
    }

}

val loggingMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
    println("Action => $action")
    next.dispatch(action)
    println("New state => ${store.state}")
}

val validActionMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
    if (action !is Action) {
        println("Only allow action objects")
        null
    } else {
        next.dispatch(action)
        action
    }
}


interface Action

interface NetworkAction: Action