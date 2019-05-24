package com.gofficer.codenames.screens.play

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.actors.Card
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.utils.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.gofficer.codenames.assets.AssetPaths
//import com.gofficer.codenames.redux.actions.CardPressed
import redux.api.Store
import com.gofficer.codenames.redux.actions.ChangeScene
import com.gofficer.codenames.redux.actions.SetupGame
import com.gofficer.codenames.redux.actions.TouchCard
import gofficer.codenames.redux.game.GameState
import ktx.assets.disposeSafely


class PlayRenderer(private val myFont: BitmapFont, private val assetManager: AssetManager,
                   private val store: Store<GameState>) : Disposable {

    companion object {
        @JvmStatic
        private val log = logger<PlayRenderer>()
    }

    // == properties ==
    val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    val uiCamera = OrthographicCamera()
    private val uiViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)
    private val renderer = ShapeRenderer()
    val batch = SpriteBatch()
    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]

    private val uiSkinAtlas = assetManager[AssetDescriptors.UI_SKIN]

    private var skin: Skin = Skin()

    private val stage: Stage = Stage(viewport)

    private var totalCards = 0


    private var subscription: Store.Subscription? = null

    // == public functions ==

    fun show() {
        stage.clear()
        log.debug("Show playrenderer")

        initSkin()

        subscription?.unsubscribe()
        subscription = store.subscribe {
            if (store.state.cards.size != totalCards) {
                showCards()
            }
        }
//        showCards()

        Gdx.input.inputProcessor = stage

    }


    private fun showCards() {
        log.debug("Store update")

        val table = Table()

        val currentState = store.state
        totalCards = currentState.cards.size

        log.debug("Card size: ${totalCards}")
        if (totalCards >= 25) {

            table.add(makeButton("Rematch") {
                log.debug("Pressed new game")
                store.dispatch(SetupGame())
            })
            table.add(makeButton("View Key") {
                store.dispatch(ChangeScene("KeyCode"))
            })
            table.add(makeButton("Quit Game") {
                log.debug("Pressed quit game")
                // TODO dispatch instead
                store.dispatch(ChangeScene("MainMenu"))
            })
            for (j in 0..4) {
                table.row().pad(10f) // padding on all sides between cards
                for (k in 1..5) {
//                val cardDrawable = TextureRegionDrawable(TextureRegion(cardTextcure))
//                val playButton = ImageButton(cardDrawable)
//                table.add(ImageTextButton("test", playButton))

//                val myCard = Image(cardTexture).apply {
//                    scaleX = 0.5f
//                    scaleY = 0.5f
//                }
                    val cardName = "test-$j-$k"
                    val id = j * 5 + k
                    val card = currentState.cards[id - 1]
                    val myCard = Card(card.id, card.text, card.type, card.isRevealed, assetManager, myFont, store)
//                myCard.touchable = Touchable.enabled
                    myCard.addListener(object : ClickListener() {
                        override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                            log.debug("Touched $cardName")

                            val duration = 0.5f
//                            myCard.addAction(SequenceAction(
//                                    FlipAction.flipOut(myCard.x, myCard.width, duration / 2),
//                                    FlipAction.flipIn(myCard.x, myCard.width, duration / 2)
//                            ))
//                            store.dispatch(CardPressed(id, cardName))
                            store.dispatch(TouchCard(id))
                            // TODO: If possible...
                            // TODO: dispatch action saying pressed
                            // TODO: have action update state tree saying item is pressed
                            // TODO: Update stage accordingly
//                        myCard.setCardText("Pushed")
//                        doSomething(x, y, pointer)pointer
                            event.handle()//the Stage will stop trying to handle this event
                            return true //the inputmultiplexer will stop trying to handle this touch
                        }
                    })
                    table.add(myCard)
                }
            }

            table.setFillParent(true)

            stage.addActor(table)
        }
    }


    fun render(delta: Float) {
//        batch.totalRenderCalls = 0

        // handle debug camera controller
//        debugCameraController.handleDebugInput()
//        debugCameraController.applyTo(camera)camera

        clearScreen()

//        if (Gdx.input.isTouched && !controller.gameOver) {
//            val screenTouch = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
//            val worldTouch = viewport.unproject(Vector2(screenTouch))
//
//            log.debug("screenTouch= $screenTouch")
//            log.debug("worldTouch= $worldTouch")
//
//            val player = controller.player
//            worldTouch.x = MathUtils.clamp(worldTouch.x, 0f, GameConfig.WORLD_WIDTH - Player.SIZE)
//            player.x = worldTouch.x
//        }

        renderGamePlay(delta)
//        renderDebug(delta)
//        renderUi(delta)

//        log.debug("totalRenderCalls= ${batch.totalRenderCalls}")
    }

    private fun renderGamePlay(delta: Float) {
        viewport.apply()
        batch.projectionMatrix = camera.combined

        stage.act(delta)
        stage.draw()
//        batch.use {
//            batch.draw(cardTexture, 0f, 0f, 260f / 2, 166f / 2)
//
//        }
    }

    private fun renderDebug(delta: Float) {
        viewport.apply()
        batch.projectionMatrix = camera.combined

//        viewport.drawGrid(renderer, (GameConfig.WORLD_WIDTH / 10).toInt())
    }

    private fun renderUi(delta: Float) {
        uiViewport.apply()
        batch.projectionMatrix = uiCamera.combined
    }

    fun resize(width: Int, height: Int) {
        log.debug("resize")
        viewport.update(width, height, true)
        uiViewport.update(width, height, true)
    }

    override fun dispose() {
        log.debug("dispose")
        stage.actors.forEach {
            it.remove()
        }
        subscription?.unsubscribe()
        renderer.disposeSafely()
//        renderer?.dispose()
        batch.disposeSafely()
//        batch.dispose()
    }

    private fun initSkin() {
        skin.addRegions(uiSkinAtlas)
        skin.add("default-font", myFont)
        skin.load(AssetPaths.UI_SKIN_JSON.toInternalFile())
    }

    private fun makeButton(name: String, onClick: () -> Unit): TextButton {
        return TextButton(name, skin, "default").apply {
            // TODO: figure out how to better deal with font, as-is this distorts bitmap
//            label.setFontScale(1f)
//            setSize(GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 5)
//            setPosition(GameConfig.WORLD_CENTER_X, positionY, Align.center)
//            addAction(sequence(alpha(0f), parallel(fadeIn(.5f), moveBy(0f, -20f, .5f, Interpolation.pow5Out))))
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    onClick()
                }
            })
        }
    }
}