package com.gofficer.codenames


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.systems.client.ClientNetworkSystem
import com.gofficer.sampler.samples.OrthographicCameraSample
import ktx.app.KtxApplicationAdapter
import ktx.app.KtxInputAdapter
import java.io.IOException
import kotlin.concurrent.thread

/**
 * This class is responsible for sending generic outbound messages to the server, as well as handling an Entity ID
 * pairmap that it uses to create and track entities sent from the server. Whenever a message comes in from the server
 * that has a component referencing an unknown Entity, the Entity is created and added to the world automatically.
 */
class GameClient : KtxApplicationAdapter, KtxInputAdapter {

    var world: GameWorld? = null

    private lateinit var clientNetworkSystem: ClientNetworkSystem


    lateinit private var multiplexer: InputMultiplexer
    var server: GameServer? = null
    private var serverThread: Thread? = null

    lateinit var stage: Stage

    lateinit var viewport: FitViewport

    override fun create() {
        // for debugging kryonet
        if (GameSettings.networkLog) {
//            Log.set(Log.LEVEL_DEBUG)
        }

        Thread.currentThread().name = "client render thread (GL)"

//        dragAndDrop = DragAndDrop()
        viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)

        //load before stage
//        VisUI.load(oreSkin)
//        TooltipManager.getInstance().apply {
//            initialTime = 0f
//            hideAll()
//        }

        stage = Stage(viewport)
//        rootTable = VisTable()
//        rootTable.setFillParent(true)
//        stage.addActor(rootTable)

        multiplexer = InputMultiplexer(stage, this)
        Gdx.input.inputProcessor = multiplexer

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
        loadingScreen = LoadingScreen(this, stage, rootTable)

        sidebar = Sidebar(stage, this)

        inGameState = State(type = GuiState.LoadingScreen,
            enter = {
                rootTable.add(chatDialog.container)
                    .expand().bottom().left()
                    .padBottom(5f).size(400f, 200f)
            },
            exit = { rootTable.clear() })

        loadingScreenState = State(type = GuiState.LoadingScreen,
            enter = {
                /*severe hack*/
                rootTable.clear()
                rootTable.add(loadingScreen).fill().expand()
            },
            exit = { rootTable.clear() })
        guiStates.push(loadingScreenState)

        startClientHostedServerAndJoin()
    }










    /**
     * immediately hops into hosting and joining its own local server
     */
    private fun startClientHostedServerAndJoin() {

        server = GameServer()
        serverThread = thread(name = "main server thread") { server!!.run() }

        try {
            //wait for the local server thread to report that it is live and running, before we attempt
            // a connection to it
            server!!.connectHostLatch.await()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        world = GameWorld(this, server, GameWorld.WorldInstanceType.ClientHostingServer)
        world!!.init()
        world!!.artemisWorld.inject(this)

        clientNetworkSystem.addListener(NetworkConnectListener(this))

        try {
            clientNetworkSystem.connect("127.0.0.1", Network.PORT)
        } catch (e: IOException) {
            e.printStackTrace()
            //fuck. gonna have to show the fail to connect dialog.
            //could be a socket error..or anything, i guess
            System.exit(1)
        }

        //showFailToConnectDialog();
    }

    private class NetworkConnectListener(private val client: GameClient) : ClientNetworkSystem.NetworkClientListener {

        override fun connected() {
            //todo surely there's some first-time connection stuff we must do?
        }

        override fun disconnected(disconnectReason: Network.Shared.DisconnectReason) {
            //todo show gui, say we've disconnected
        }

    }

    companion object {
        val VERSION_MAJOR = 0
        val VERSION_MINOR = 1
        val VERSION_REVISION = 1
    }
}