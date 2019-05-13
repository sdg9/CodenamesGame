package com.gofficer.codenames

import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Logger
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.redux.actions.ChangeScene
import com.gofficer.codenames.redux.actions.NetworkAction
import com.gofficer.codenames.redux.actions.NetworkMessage
import com.gofficer.codenames.redux.actions.SetupCards
import com.gofficer.codenames.redux.createCodeNamesStore
import com.gofficer.codenames.redux.middleware.loggingMiddleware
import com.gofficer.codenames.redux.middleware.setupGameMiddleware
import com.gofficer.codenames.redux.middleware.validActionMiddleware
import com.gofficer.codenames.redux.models.Card
import com.gofficer.codenames.redux.models.CardPressed
import com.gofficer.codenames.redux.models.boardReduceSetup
import com.gofficer.codenames.redux.models.cardReduce
import com.gofficer.codenames.redux.reducers.reduceGameSetup
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.screens.menu.MainMenuScreen
import com.gofficer.codenames.screens.play.PlayScreen
import com.gofficer.codenames.utils.logger
import com.gofficer.colyseus.client.Client
import com.gofficer.colyseus.client.Room
import com.gofficer.sampler.utils.toInternalFile
import gofficer.codenames.redux.game.GameState
//import io.colyseus.Client
import redux.api.Store
//import io.colyseus.Room
//import io.colyseus.state_listener.PatchListenerCallback
//import io.colyseus.state_listener.DataChange
//import io.colyseus.state_listener.PatchObject
//import io.colyseus.state_listener.FallbackPatchListenerCallback
import redux.api.Dispatcher
import redux.api.enhancer.Middleware
import java.util.*


class CodenamesGame : Game() {

    companion object {
        @JvmStatic
        private val log = logger<CodenamesGame>()
    }

    var room: Room? = null
    var client: Client? = null
    val assetManager = AssetManager()
    lateinit var font24: BitmapFont
    private val initState: GameState = GameState()
    internal lateinit var store: Store<GameState>

//    private val endpoint = "ws://10.0.2.2:2567" //ws://localhost:2567
//    private val endpoint = "ws://127.0.0.1:2567" //ws://localhost:2567
//    private val endpoint = "ws://localhost:2567" //ws://localhost:2567

    private val checkLatencyInterval = 10000

    private var lastLatencyCheckTime: Long = 0

    private val lastNetworkUpdateTime: Long = 0

    private val LATENCY_MIN = 100f // ms
    private val LATENCY_MAX = 500f // ms

    private val LERP_MIN = 0.1f
    private val LERP_MAX = 0.5f
    private var lerp = LERP_MAX
    private lateinit var game: CodenamesGame

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        assetManager.logger.level = Logger.DEBUG
        log.debug("create")

        initFonts()

        game = this

        createReduxStore()
        setScreen(LoadingScreen(game))
    }

    private fun createReduxStore() {

        store = createCodeNamesStore(initState,
            arrayOf(
                reduceGameSetup,
                boardReduceSetup,
                cardReduce
            ),
            arrayOf(
                loggingMiddleware,
                validActionMiddleware,
                setupGameMiddleware{ client != null },
                getNetworkActionMiddleware(game),
                getNavigationMiddleware(game)
            )
        )
    }

    fun connectToServer(onOpenCallback: (() -> Unit)? = null) {
        log.debug("Attempting connection to server")
        val endpoint = if (Gdx.app.type == Application.ApplicationType.Android) GameConfig.LOCAL_WEBSOCKET_ANDROID else GameConfig.LOCAL_WEBSOCKET_DESKTOP
        client = Client(endpoint, object : Client.Listener {
//            override fun onOpen(id: String?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }

            override fun onOpen(id: String?) {
                log.debug("onOpen called")
                room = client?.join("public")
                log.debug("Joined room: $room")

                println("Client.onOpen();")
                println("colyseus id: $id")

//                room?.addPatchListener("players/:id", object : PatchListenerCallback() {
//                    override fun callback(change: DataChange) {
//                        log.debug("patchListener: $change")
//                    }
//                })
//                room?.setDefaultPatchListener(object : FallbackPatchListenerCallback() {
//                    override fun callback(patch: PatchObject) {
//                    }
//                })
                room?.addListener(object : Room.Listener() {

                    override fun onMessage(message: Any) {
                        log.debug("onMessage: $message")
                        if (message == "pong") {
                            calculateLerp((System.currentTimeMillis() - lastLatencyCheckTime).toFloat())
                        }

                        if (message is LinkedHashMap<*, *>) {
                            val type = message["type"]

                            if (type == CardPressed::class.java.simpleName) {

                                val payload: LinkedHashMap<*, *> = message["payload"] as LinkedHashMap<*, *>
                                val id = payload["id"] as Int
                                val word = payload["word"] as String

                                log.debug("Type is $type")
                                log.debug("Payload is $payload")
                                log.debug("id is $id")
                                log.debug("word is $word")


                                store.dispatch(CardPressed(id, word, true))
                            }
                            if (type == "GameSetup") {
                                log.debug("Game setup found")
                                try {

                                    val payload: LinkedHashMap<*, *> = message["payload"] as LinkedHashMap<*, *>
                                    val cardsFromServer = payload["cards"] as List<LinkedHashMap<*, *>>
                                    val cards = cardsFromServer.map {
                                        Card(it["id"] as Int, it["text"] as String, it["type"] as String, it["isRevealed"] as Boolean)
                                    }

                                    log.debug("Cards $cards")
                                    log.debug("Payload: $cards")

                                    store.dispatch(SetupCards(cards))
                                } catch (e: Exception) {
                                    log.error(e.toString())
                                }
                            }

                        } else {
                            log.debug("Message is not string $message")


                            log.debug("Message type is ${message?.javaClass?.kotlin}")
                        }
                    }
                })

                onOpenCallback?.invoke()
            }

            override fun onMessage(message: Any) {
                log.debug("Client.onMessage(): $message")


            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                println("Client.onClose();")
            }

            override fun onError(e: Exception) {
                println("Client.onError()")
                e.printStackTrace()
            }
        })
        log.debug("Client: $client")
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

    private fun checkLatency() {
        log.debug("Room: $room")
        if (room == null) return
        val data = LinkedHashMap<String, String>()
        data["op"] = "ping"
        log.debug("Sending message to server")
        room?.send(data)
    }


    private fun calculateLerp(currentLatency: Float) {
        val latency: Float
        if (currentLatency < LATENCY_MIN)
            latency = LATENCY_MIN
        else if (currentLatency > LATENCY_MAX)
            latency = LATENCY_MAX
        else
            latency = currentLatency
        lerp = LERP_MAX + (latency - LATENCY_MIN) / (LATENCY_MAX - LATENCY_MIN) * (LERP_MIN - LERP_MAX)
        println("current latency: $currentLatency ms")
        System.out.println("lerp : ${lerp}")
    }

}

val getNetworkActionMiddleware = { game: CodenamesGame ->
    Middleware  { store: Store<GameState>, next: Dispatcher, action: Any ->
        if (action is NetworkAction && !action.isFromServer && game.room != null) {
            println("Dispatching remotely: $action")

//            val gsonBuilder = GsonBuilder()
//            gsonBuilder.registerTypeAdapter(action::class.java, MenuContentInterfaceAdapter())
//            var gson = gsonBuilder.create()

//            var gson = Gson()
//            var jsonString = gson.toJson(NetworkMessage(action::class.java.simpleName, action))

//            log.debug("Sending $jsonString")
//            room?.send(action.toJson())
            game.room?.send(NetworkMessage(action::class.java.simpleName, action))
        }
        next.dispatch(action)
    }
}


val getNavigationMiddleware = { game: CodenamesGame ->
    Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->

//        println("Changing Screen $action")
        if (action is ChangeScene) {
            when (action.screenName) {
                "MainMenu" -> {
//                    Gdx.app.postRunnable {
                    println("Calling close on client")
                    game.room?.leave()
                    game.client?.close()
                    game.screen = MainMenuScreen(game)
                }
                "PlayOnline" -> {
                    game.connectToServer {
                        Gdx.app.postRunnable {
                            println("Online play")
                            game.screen = PlayScreen(game)
                        }
                    }
                }
                "Play" -> {
//                    Gdx.app.postRunnable {
                    game.screen = PlayScreen(game)
//                    }
                }
                else -> null
            }
        } else {
            next.dispatch(action)
        }
    }
}