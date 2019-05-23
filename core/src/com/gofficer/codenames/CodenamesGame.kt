package com.gofficer.codenames

import com.badlogic.ashley.core.*
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Logger
import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.redux.actions.*
import com.gofficer.codenames.redux.createCodeNamesStore
import com.gofficer.codenames.redux.middleware.loggingMiddleware
import com.gofficer.codenames.redux.middleware.setupGameMiddleware
import com.gofficer.codenames.redux.middleware.validActionMiddleware
import com.gofficer.codenames.redux.reducers.reduceGameSetup
import com.gofficer.codenames.redux.utils.actionToNetworkBytes
import com.gofficer.codenames.redux.utils.protocolToAction
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.screens.loading.SplashScreen
import com.gofficer.codenames.screens.menu.MainMenuScreen
import com.gofficer.codenames.screens.play.KeyCodeScreen
import com.gofficer.codenames.screens.play.PlayScreen
import com.gofficer.codenames.utils.logger
import com.gofficer.colyseus.client.Client
import com.gofficer.colyseus.client.Room
import com.gofficer.colyseus.network.Protocol
import com.gofficer.colyseus.network.ProtocolMessage
import com.gofficer.colyseus.network.unpackUnknown
import com.gofficer.sampler.utils.toInternalFile
import gofficer.codenames.redux.game.GameState
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.ashley.allOf
import ktx.ashley.entity
import ktx.ashley.exclude
import ktx.ashley.mapperFor
import ktx.log.info
import org.w3c.dom.Text
import redux.api.Store
import redux.api.Dispatcher
import redux.api.enhancer.Middleware
import java.awt.TextComponent
import java.util.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.gofficer.codenames.utils.use
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.Input.Buttons
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Family
import com.badlogic.gdx.graphics.OrthographicCamera




class CodenamesGame : KtxGame<KtxScreen>() {

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

    private val LATENCY_MIN = 100f // ms
    private val LATENCY_MAX = 500f // ms

    private val LERP_MIN = 0.1f
    private val LERP_MAX = 0.5f
    private var lerp = LERP_MAX
    private lateinit var game: CodenamesGame

    val engine = Engine()

    private val moshiPack = MoshiPack()

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        assetManager.logger.level = Logger.DEBUG
        log.debug("create")

        initFonts()

        game = this

        createReduxStore()
//        setScreen(LoadingScreen(game))

        addScreen(LoadingScreen(game))
//        addScreen(PlayScreen(game))
//        addScreen(MainMenuScreen(game))
//        addScreen(SplashScreen(game))

        setScreen<LoadingScreen>()

        engine.addSystem(SomeSystem())

//        createEntities()
    }


    override fun render() {
        super.render()
        engine.update(Gdx.graphics.deltaTime)
    }

    private fun createReduxStore() {

        store = createCodeNamesStore(initState,
            arrayOf(
                reduceGameSetup
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

                room?.addListener(object : Room.Listener() {

                    override fun onStateChange(message: Any){

                        val protocolMessage = unpackUnknown(message)

                        when (protocolMessage?.protocol) {
                            Protocol.ROOM_DATA -> {
                                log.debug("onStateChange Room Data $message")
                            }
                            Protocol.ROOM_STATE -> {
                                log.debug("onStateChange Room State $message")
                            }
                            else -> {
                                log.debug("onStateChange other $message")
                            }

                        }
                    }


                    override fun onMessage(protocolMessage: ProtocolMessage) {
                        if (protocolMessage == null) {
                            log.error("Cannot read protocol $protocolMessage")
                        }

                        when (protocolMessage?.protocol) {
                            Protocol.ROOM_DATA -> {
//                                log.debug("onMessage Room Data $protocolMessage")
                                val action = protocolToAction(protocolMessage)
                                log.debug("onMessage.ROOM_DATA - Received action: $action")
                                action?.isFromServer = true
                                store.dispatch(action)
                            }
                            Protocol.ROOM_STATE -> {
                                log.debug("onMessage.ROOM_STATE - ${protocolMessage?.message}")
                                val message = protocolMessage?.message
                                if (message != null) {
                                    val state = moshiPack.unpack<GameState>(message)
                                    val action = SetState(state)
                                    action.isFromServer = true
                                    store.dispatch(action)
                                }
                            }
                            else -> {
                                log.debug(protocolMessage?.protocol!!::class.simpleName)
                                log.debug("onMessage other $protocolMessage")
                            }
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

//    private fun checkLatency() {
//        log.debug("Room: $room")
//        if (room == null) return
//        val data = LinkedHashMap<String, String>()
//        data["op"] = "ping"
//        log.debug("Sending message to server")
//        room?.send(data)
//    }


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
        println("getNetworkMiddleware $action")
        if (action is NetworkAction && !action.isFromServer) {
            println("Dispatching remotely: $action")
            if (game.room == null) {
                println("No connected game room to dispatch network action")
            } else {
                game.room?.send(actionToNetworkBytes(action))
            }
        }
        next.dispatch(action)
    }
}


val getNavigationMiddleware = { game: CodenamesGame ->
    Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
        println("getNavigationMiddleware $action")
//        println("Changing Screen $action")
        if (action is ChangeScene) {
            when (action.screenName) {
                "MainMenu" -> {
//                    Gdx.app.postRunnable {
                    println("Calling close on client")
                    game.room?.leave()
                    game.client?.close()
//                    game.screen = MainMenuScreen(game)
                    game.setScreen<MainMenuScreen>()
                }
                "PlayOnline" -> {
                    game.connectToServer {
                        Gdx.app.postRunnable {
                            println("Online play")
//                            game.screen = PlayScreen(game)
                            game.setScreen<PlayScreen>()
                        }
                    }
                }
                "PlayScreen" -> {
                    // Assumes game already open
//                    game.screen = PlayScreen(game)
                    game.setScreen<PlayScreen>()
                }
                "KeyCode" -> {
                    // Assumes game already open
//                    game.screen = KeyCodeScreen(game)
                    game.setScreen<KeyCodeScreen>()
                }
                "Play" -> {
//                    Gdx.app.postRunnable {
//                    game.screen = PlayScreen(game)
                    game.setScreen<PlayScreen>()
//                    }
                }
                else -> null
            }
        } else {
            next.dispatch(action)
        }
    }
}


class Transform(var position: Vector2) : Component {
}

class TextureComponent(var texture: TextureRegion?) : Component {

}

class Revealable(var isRevealed: Boolean = false) : Component

class Clickable(var bounds: Rectangle = Rectangle(0f, 0f, 100f, 100f)) : Component

//class Texture: Component
//class Transform: Component
//class RigidBody: Component

val transform  = mapperFor<Transform>()
val texture  = mapperFor<TextureComponent>()
val revealable = mapperFor<Revealable>()
val clickable = mapperFor<Clickable>()
val family1 = Family.all(Transform::class.java, TextureComponent::class.java).get()
val family2 = allOf(Transform::class, TextureComponent::class)
//var family = allOf(Texture::class, Transform::class).exclude(RigidBody::class)

class RenderingSystem(val batch: SpriteBatch) : IteratingSystem(family2.get()) {

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val img = texture[entity].texture
        val position = transform[entity].position
        val isRevealed = revealable[entity]?.isRevealed
//        info { "Entity being processed $entity $position"}

        batch.use {
            batch.color = if (isRevealed == true) Color.BLUE else Color.WHITE
            batch.draw(img, position.x, position.y)
            batch.color = Color.WHITE
        }

    }
}

class TouchSystem(private val camera: OrthographicCamera) : IteratingSystem(allOf(Clickable::class, Transform::class).get()) {

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val bounds = clickable[entity].bounds
        val position = transform[entity].position
        bounds.x = position.x
        bounds.y = position.y

        if(Gdx.input.isButtonPressed(Buttons.LEFT)){
            // touching

            val clickPosition = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            info { "Clicked: $clickPosition, but bounds: $bounds" }
            if (bounds.contains(clickPosition.x, clickPosition.y)) {
                info { "Touched $entity"}
                revealable[entity]?.isRevealed = true
            }
        }

    }
}

class SomeSystem : EntitySystem() {
    override fun update(deltaTime: Float) {
//        info { "T $deltaTime"}
    }

}
