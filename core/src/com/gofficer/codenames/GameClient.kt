package com.gofficer.codenames


import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Logger
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.systems.client.clientNetworkSystem
import com.gofficer.codenames.systems.client.ClientNetworkSystemOld
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
//
//    private lateinit var ClientNetworkSystemOld: ClientNetworkSystemOld
//
    private lateinit var clientNetworkSystem: clientNetworkSystem

    fun sendToAll(obj: Any) {
        clientNetworkSystem.kryonetClient.sendToAll(obj)
    }

    val assetManager = AssetManager()
    private var gameplayAtlas :TextureAtlas? = null
    var cardTexture : TextureRegion? = null

    lateinit var font24: BitmapFont

    var server: GameServer? = null
    private var serverThread: Thread? = null

    lateinit var stage: Stage

    lateinit var viewport: FitViewport




    override fun create() {
        // for debugging kryonet
        if (GameSettings.networkLog) {
//            Log.set(Log.LEVEL_DEBUG)
        }
        Gdx.app.logLevel = Application.LOG_DEBUG
        assetManager.logger.level = Logger.DEBUG

        initFonts()

        Thread.currentThread().name = "client render thread (GL)"

//        dragAndDrop = DragAndDrop()
//        viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)

        //load before stage
//        VisUI.load(oreSkin)
//        TooltipManager.getInstance().apply {
//            initialTime = 0f
//            hideAll()
//        }

//        stage = Stage(viewport)
//        rootTable = VisTable()
//        rootTable.setFillParent(true)
//        stage.addActor(rootTable)

//        multiplexer = InputMultiplexer(stage, this)
//        Gdx.input.inputProcessor = multiplexer

        addScreen(LoadingScreen(this))
        setScreen<LoadingScreen>()

//        //fixme: this really needs to be stripped out of the client, put in a proper
//        //system or something
//        fontGenerator = FreeTypeFontGenerator(file("fonts/Ubuntu-L.ttf"))
//        val parameter = FreeTypeFontGenerator.FreeTypeFontParameter()
//        parameter.size = 13
//        bitmapFont_8pt = fontGenerator.generateFont(parameter)
//
//        fontGenerator.dispose()
//
//        chatDialog = ChatDialog(this, stage, rootTable)
//        multiplexer.addProcessor(chatDialog.inputListener)
//
//        chat = Chat()
//        chat.addListener(chatDialog)
//
//        hud = Hud(this, stage, rootTable)
//        loadingScreen = LoadingScreen(this, stage, rootTable)
//
//        sidebar = Sidebar(stage, this)
//
//        inGameState = State(type = GuiState.LoadingScreen,
//            enter = {
//                rootTable.add(chatDialog.container)
//                    .expand().bottom().left()
//                    .padBottom(5f).size(400f, 200f)
//            },
//            exit = { rootTable.clear() })
//
//        loadingScreenState = State(type = GuiState.LoadingScreen,
//            enter = {
//                /*severe hack*/
//                rootTable.clear()
//                rootTable.add(loadingScreen).fill().expand()
//            },
//            exit = { rootTable.clear() })
//        guiStates.push(loadingScreenState)

//        startClientHostedServerAndJoin()
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
//        gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
//        cardTexture = gameplayAtlas!![RegionNames.CARD]


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

        log.debug { "Creating client world"}
        world = GameWorld(this, server, GameWorld.WorldInstanceType.ClientHostingServer)
        log.debug { "Initializing client hosted server client "}
        world!!.init()
        world!!.artemisWorld.inject(this)


//        if (listener != null) {
//            log.debug { "Adding client listener "}
//            clientNetworkSystem.kryonetClient.add
//            clientNetworkSystem.kryonetClient.setListener(listener)
////            clientNetworkSystem.addListener(listener)
//        }

        clientNetworkSystem.kryonetClient.start()
////
//        try {
//            clientNetworkSystem.connect("127.0.0.1", Network.PORT)
//        } catch (e: IOException) {
//            e.printStackTrace()
//            //fuck. gonna have to show the fail to connect dialog.
//            //could be a socket error..or anything, i guess
//            System.exit(1)
//        }

        //showFailToConnectDialog();
    }

    fun joinExistingServer(listener: ClientNetworkSystemOld.NetworkClientListener) {
        gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
        cardTexture = gameplayAtlas!![RegionNames.CARD]

        world = GameWorld(this, null, GameWorld.WorldInstanceType.ClientHostingServer)
        log.debug { "Initializing joining existing server client"}
        world!!.init()
        // Injects now instantiated clientNetworkSystem to this class
        world!!.artemisWorld.inject(this)

//        if (listener != null) {
//            log.debug { "Adding client listener "}
//            clientNetworkSystem.addListener(listener)
//        }
//
//        try {
//            clientNetworkSystem.connect("127.0.0.1", Network.PORT)
//        } catch (e: IOException) {
//            e.printStackTrace()
//            //fuck. gonna have to show the fail to connect dialog.
//            //could be a socket error..or anything, i guess
//            System.exit(1)
//        }
    }


//    private class NetworkConnectListener(private val client: GameClient) : ClientNetworkSystemOld.NetworkClientListener {
//
//        override fun connected() {
//            //todo surely there's some first-time connection stuff we must do?
//        }
//
//        override fun disconnected(disconnectReason: Network.Shared.DisconnectReason) {
//            //todo show gui, say we've disconnected
//        }
//
//    }


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