package com.gofficer.codenames

import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Logger
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.models.CardPressed
import com.gofficer.codenames.models.boardReduceSetup
import com.gofficer.codenames.models.cardReduce
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.screens.menu.MainMenuScreen
import com.gofficer.codenames.screens.play.PlayScreen
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
import io.colyseus.state_listener.PatchListenerCallback
import io.colyseus.state_listener.DataChange
import io.colyseus.state_listener.PatchObject
import io.colyseus.state_listener.FallbackPatchListenerCallback

import java.util.LinkedHashMap


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

//        connectToServer()

        game = this

        createReduxStore()
        setScreen(LoadingScreen(game))
    }

    private fun createReduxStore() {
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
                        navigationMiddleware(game),
                        setupGameMiddleware
                ))
    }

    fun connectToServer(onOpenCallback: (() -> Unit)? = null) {
        log.debug("Attempting connection to server")
        val endpoint = if (Gdx.app.type == Application.ApplicationType.Android) GameConfig.LOCAL_WEBSOCKET_ANDROID else GameConfig.LOCAL_WEBSOCKET_DESKTOP
        client = Client(endpoint, object : Client.Listener {
            override fun onOpen(id: String) {
                log.debug("onOpen called")
                room = client?.join("public")
                log.debug("Joined room: $room")

                println("Client.onOpen();")
                println("colyseus id: $id")

                room?.addPatchListener("players/:id", object : PatchListenerCallback() {
                    override fun callback(change: DataChange) {
                        log.debug("patchListener: $change")
                        //                        System.out.println(">>> players/:id");
                        //                        System.out.println(change.path);
                        //                        System.out.println(change.operation);
                        //                        System.out.println(change.value);
//                        if (change.operation == "add") {
//                            val player = Player()
//                            val data = change.value as LinkedHashMap<String, Any>
//
//                            if (data["x"] is Float)
//                                player.position.x = data["x"] as Float
//                            else if (data["x"] is Double)
//                                player.position.x = (data["x"] as Double).toFloat()
//                            else if (data["x"] is Int)
//                                player.position.x = (data["x"] as Int).toFloat()
//
//                            if (data["y"] is Float)
//                                player.position.y = data["y"] as Float
//                            else if (data["y"] is Double)
//                                player.position.y = (data["y"] as Double).toFloat()
//                            else if (data["y"] is Int)
//                                player.position.y = (data["y"] as Int).toFloat()
//
//                            if (data["radius"] is Float)
//                                player.radius = data["radius"] as Float
//                            else if (data["radius"] is Double)
//                                player.radius = (data["radius"] as Double).toFloat()
//                            else if (data["radius"] is Int)
//                                player.radius = (data["radius"] as Int).toFloat()
//
//                            val color: Int
//                            if (data["color"] is Long)
//                                color = (data["color"] as Long).toInt()
//                            else
//                                color = data["color"] as Int
//                            player.color = Color(color)
//                            player.strokeColor = Color(player.color)
//                            player.strokeColor.mul(0.9f)
//
//                            players.put(change.path["id"], player)
//                        } else if (change.operation == "remove") {
//                            players.remove(change.path["id"])
//                        }
                    }
                })
                room?.setDefaultPatchListener(object : FallbackPatchListenerCallback() {
                    override fun callback(patch: PatchObject) {
                        log.debug("Default listener: $patch")
                        //                        System.out.println(" >>> default listener");
                        //                        System.out.println(patch.path);
                        //                        System.out.println(patch.operation);
                        //                        System.out.println(patch.value);
                    }
                })
                room?.addListener(object : Room.Listener() {
                    override fun onMessage(message: Any?) {
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


                                store.dispatch(CardPressed(id, word))
                            }

//                            val action = Gson().fromJson(payload, CardPressed::class.java)
//                            val action = Gson().fromJson(message, NetworkMessage::class.java)
//                            log.debug("Message is ${action.payload}")
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
//                try {
//                    Thread.sleep(2000)
////                    players.clear()
////                    fruits.clear()
////                    connectToServer()
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }

            }

            override fun onError(e: Exception) {
                println("Client.onError()")
                e.printStackTrace()
            }
        })
        log.debug("Client: $client")


    }


    val networkActionMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
        if (action is NetworkAction && room != null) {
            println("Dispatching remotely: $action")

//            val gsonBuilder = GsonBuilder()
//            gsonBuilder.registerTypeAdapter(action::class.java, MenuContentInterfaceAdapter())
//            var gson = gsonBuilder.create()

//            var gson = Gson()
//            var jsonString = gson.toJson(NetworkMessage(action::class.java.simpleName, action))

//            log.debug("Sending $jsonString")
//            room?.send(action.toJson())
            room?.send(NetworkMessage(action::class.java.simpleName, action))
        }
        next.dispatch(action)
    }

//    override fun render() {
////        super.render()
//        val now = System.currentTimeMillis()
//        if (now - lastLatencyCheckTime >= checkLatencyInterval) {
//            log.debug("Calling ping")
//            lastLatencyCheckTime = now
//            checkLatency()
////            log.debug("R: ${client.rooms}")
//        }
//    }

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

interface NetworkAction : Action {
//    fun toJson(): String
}

data class NetworkMessage(val type: String?, val payload: NetworkAction)

data class ChangeScene(val screenName: String) : Action


val navigationMiddleware = { game: CodenamesGame ->
    Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->

        println("Changing Screen $action")
        if (action is ChangeScene) {
            when (action.screenName) {
                "MainMenu" -> {
//                    Gdx.app.postRunnable {
                    println("Calling close on client")
                    game.room?.leave()
                    game.client?.close()
//                    game.client?.close()
//                    game.client = null
                    game.screen = MainMenuScreen(game)
//                    }
                }
                "PlayOnline" -> {
                    game.connectToServer {
                        Gdx.app.postRunnable {
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
//class MenuContentInterfaceAdapter : JsonDeserializer<Any>, JsonSerializer<Any> {
//
//    companion object {
//        const val CLASSNAME = "CLASSNAME"
//        const val DATA  = "DATA"
//    }
//
//    @Throws(JsonParseException::class)
//    override fun deserialize(jsonElement: JsonElement, type: Type,
//                             jsonDeserializationContext: JsonDeserializationContext): Any {
//
//        val jsonObject = jsonElement.asJsonObject
//        val prim = jsonObject.get(CLASSNAME) as JsonPrimitive
//        val className = prim.asString
//        val objectClass = getObjectClass(className)
//        return jsonDeserializationContext.deserialize(jsonObject.get(DATA), objectClass)
//    }
//
//    override fun serialize(jsonElement: Any, type: Type, jsonSerializationContext: JsonSerializationContext): JsonElement {
//        val jsonObject = JsonObject()
//        jsonObject.addProperty(CLASSNAME, jsonElement.javaClass.name)
//        jsonObject.add(DATA, jsonSerializationContext.serialize(jsonElement))
//        return jsonObject
//    }
//
//    private fun getObjectClass(className: String): Class<*> {
//        try {
//            return Class.forName(className)
//        } catch (e: ClassNotFoundException) {
//            throw JsonParseException(e.message)
//        }
//
//    }
//}