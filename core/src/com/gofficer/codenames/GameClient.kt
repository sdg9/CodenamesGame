package com.gofficer.codenames


import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Logger
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.systems.client.ClientNetworkSystem
import com.gofficer.codenames.utils.get
import com.gofficer.sampler.utils.toInternalFile
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.log.logger
import net.mostlyoriginal.api.network.marshal.common.MarshalObserver
import kotlin.concurrent.thread

/**
 * This class is responsible for sending generic outbound messages to the server, as well as handling an Entity ID
 * pairmap that it uses to create and track entities sent from the server. Whenever a message comes in from the server
 * that has a component referencing an unknown Entity, the Entity is created and added to the world automatically.
 */
class GameClient : KtxGame<KtxScreen>() {


    var world: GameWorld? = null

    private lateinit var clientNetworkSystem: ClientNetworkSystem

    fun sendToAll(obj: Any) {
        clientNetworkSystem.kryonetClient.sendToAll(obj)
    }

    val assetManager = AssetManager()
    private var gameplayAtlas :TextureAtlas? = null
    var cardTexture : TextureRegion? = null

    lateinit var font24: BitmapFont

    var server: GameServer? = null
    private var serverThread: Thread? = null

    lateinit var viewport: FitViewport

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        assetManager.logger.level = Logger.DEBUG

        initFonts()

        Thread.currentThread().name = "client render thread (GL)"

        addScreen(LoadingScreen(this))
        setScreen<LoadingScreen>()
    }

    override fun render() {
        super.render()
        if (world != null) {
            //severe gotta be a better solution w/ coroutines
            //it's our hosted server, but it's still trying to generate the world...keep waiting
            if (server != null) {
                //severe this gets run everytime after it gets completed, trashing my framerate ;-)
                //need a better solution..
//                if (guiStates.peek() != inGameState) {
//                    loadingScreen.progressComplete()
//                    guiStates.pop()
//                    guiStates.push(inGameState)
//                }
            }
            world!!.process()
        }
    }


    /**
     * immediately hops into hosting and joining its own local server
     */
    fun startClientHostedServerAndJoin(listener: MarshalObserver?) {
        startLocalServer()
        startLocalClient(GameWorld.WorldInstanceType.ClientHostingServer)
    }

    private fun startLocalServer() {
        log.debug { "Initializing game server" }
        server = GameServer()
        log.debug { "Running game server in new thread" }
        serverThread = thread(name = "main server thread") { server!!.run() }

        try {
            //wait for the local server thread to report that it is live and running, before we attempt
            // a connection to it
            server!!.connectHostLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun startLocalClient(type: GameWorld.WorldInstanceType) {
        // TODO: Determine optimal texture approach
        gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
        cardTexture = gameplayAtlas!![RegionNames.CARD]
        log.debug { "Creating client world"}
        world = GameWorld(this, server, type)
        log.debug { "Initializing client hosted server client "}
        world!!.init()
        world!!.artemisWorld.inject(this)
        clientNetworkSystem.kryonetClient.start()
    }

    fun joinExistingServer() {
        startLocalClient(GameWorld.WorldInstanceType.Client)
    }

    private fun initFonts() {
        val generator = FreeTypeFontGenerator("fonts/Arcon.ttf".toInternalFile())
        val params = FreeTypeFontGenerator.FreeTypeFontParameter()

        params.size = 24
        params.color = Color.BLACK
        font24 = generator.generateFont(params)
    }



    companion object {
        val log = logger<GameClient>()

        val VERSION_MAJOR = 0
        val VERSION_MINOR = 1
        val VERSION_REVISION = 1
    }
}