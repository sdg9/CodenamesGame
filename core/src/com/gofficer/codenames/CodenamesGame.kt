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
import redux.api.Dispatcher
import redux.api.Store
import redux.api.enhancer.Middleware
import redux.applyMiddleware
import redux.combineReducers
import redux.createStore

class CodenamesGame : Game() {

    companion object {
        @JvmStatic
        private val log = logger<CodenamesGame>()
    }

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


val networkActionMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
    if (action is NetworkAction) {
        println("Dispatching remotely: $action")
    }
    next.dispatch(action)
}

interface Action

interface NetworkAction: Action