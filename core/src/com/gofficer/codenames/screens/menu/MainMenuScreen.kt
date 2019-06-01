package com.gofficer.codenames.screens.menu


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.AssetPaths
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.GameClient
import com.gofficer.codenames.Network
import com.gofficer.codenames.screens.play.PlayScreen
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.toInternalFile
import ktx.app.KtxScreen
import ktx.log.logger
import ktx.scene2d.*


class MainMenuScreen(private val client: GameClient) : KtxScreen {

    companion object {
        @JvmStatic
//        private val log = logger<MainMenuScreen>()
        val log = logger<MainMenuScreen>()
    }

    private var skin: Skin = Skin()

    private val renderer: ShapeRenderer = ShapeRenderer()

    private val uiCamera = OrthographicCamera()
    private val uiViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)

    private val stage: Stage = Stage(uiViewport)

    private val uiSkinAtlas = client.assetManager[AssetDescriptors.UI_SKIN]


    override fun show() {
        log.debug { "show" }
        Gdx.input.inputProcessor = stage
        stage.clear()

        initSkin()
        initButtons()
    }

    private fun update(delta: Float) {
        stage.act(delta)
    }

    override fun render(delta: Float) {
        clearScreen()

        update(delta)

        stage.draw()

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            hide()
            show()
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
        renderer.dispose()
    }

    // === private ===


    private fun initSkin() {
        skin.addRegions(uiSkinAtlas)
        skin.add("default-font", client.font24)
        skin.load(AssetPaths.UI_SKIN_JSON.toInternalFile())

        Scene2DSkin.defaultSkin = skin
    }

    private fun initButtons() {
        val buttonWidth = GameConfig.HUD_WIDTH * .6f
        val buttonHeight = GameConfig.HUD_HEIGHT / 4

        var root = container {
            setFillParent(true) // apparently same as setting size to width/height
            align(Align.top)
            padTop(30f)
            verticalGroup {
                space(8f)
                textButton("Host Game") {
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            // TODO respond to listener
                            client.startClientHostedServerAndJoin()
                            client.setScreen<PlayScreen>()
                            client.sendToAll(Network.Client.JoinRoomRequest())
//                            client.startClientHostedServerAndJoin(object : MarshalObserver {
//                                override fun disconnected(connectionId: Int) {
//
//                                }
//
//                                override fun received(connectionId: Int, `object`: Any?) {
//
//
//                                }
//
//                                override fun connected(connectionId: Int) {
//                                    log.debug { "Connected, nav to play" }
//                                    Gdx.app.postRunnable {
//                                        client.setScreen<PlayScreen>()
//                                        client.sendToAll(Network.Client.JoinRoomRequest())
//                                    }
//                              }
//                            })

//                            client.setScreen<PlayScreen>()
//                            client.startClientHostedServerAndJoin(object : ClientNetworkSystemOld.NetworkClientListener {
//                                override fun connected() {
//                                    log.debug { "Connected" }
//                                    Gdx.app.postRunnable {
//                                        client.setScreen<PlayScreen>()
//                                    }
//                                }
//                            })
                        }
                    })
                }
                space(8f)
                textButton("Connect to Server") {
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            client.joinExistingServer()
                            client.setScreen<PlayScreen>()
                            client.sendToAll(Network.Client.JoinRoomRequest())
//                            client.joinExistingServer(object : ClientNetworkSystemOld.NetworkClientListener {
//                                override fun connected() {
//                                    log.debug { "Connected" }
//                                    Gdx.app.postRunnable {
//                                        client.setScreen<PlayScreen>()
//                                    }
//                                }
//                            })
//                            val client = Client()
//                            client.client = client
//                            client.start()
//                            client.connect(5000, "127.0.0.1", Network.PORT)
//                            Network.register(client)
//                            client.engine.addSystem(ClientNetworkEntitySystem(client))
//                            val request = SomeRequest("Here is the request")
//                            client.sendTCP(request)
//
//                            client.setScreen<PlayScreen>()
//                            server.bind(Network.PORT)
//
//                            server.addListener(object : Listener() {
//                                override fun received(connection: Connection, `object`: Any) {
//                                    info { "Connection: $connection"}
////                                    if (`object` is SomeRequest) {
////                                        val request = `object` as SomeRequest
////                                        System.out.println(request.text)
////
////                                        val response = SomeResponse()
////                                        response.text = "Thanks"
////                                        connection.sendTCP(response)
////                                    }
//                                }
//                            })
                        }
                    })
                }
//                textButton("Play Online") {
//                    // TODO why do none of these sizes work?
//                    setSize(100f, 100f)
//                    height = 100f
//                    width = 100f
//                    addListener(object : ClickListener() {
//                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                            game.store.dispatch(ChangeScene("PlayOnline"))
//                        }
//                    })
//                }
//                space(8f)
//                textButton("Play Local") {
//                    setSize(buttonWidth, buttonHeight)
//                    addListener(object : ClickListener() {
//                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                            game.store.dispatch(ChangeScene("Play"))
//                        }
//                    })
//                }
                space(8f)
                textButton("Exit") {
                    setSize(buttonWidth, buttonHeight)
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            Gdx.app.exit()
                        }
                    })
                }

            }

        }

        stage.addActor(root)
//        stage.isDebugAll = true
        Gdx.input.inputProcessor = stage
    }

}

class SomeRequest(var text: String = "")
//class SomeRequest {
//    var text: String = ""
//
//    constructor() {
//    }
//
//    constructor(text: String) {
//        this.text = text
//    }
//}