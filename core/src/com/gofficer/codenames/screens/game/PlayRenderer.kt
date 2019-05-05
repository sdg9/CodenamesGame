package com.gofficer.codenames.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.actors.Card
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.utils.*
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener


class PlayRenderer(private val myFont: BitmapFont, private val assetManager: AssetManager,
                   private val controller: PlayController) : Disposable {

    companion object {
        @JvmStatic
        private val log = logger<PlayRenderer>()
    }

    // == properties ==
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    private val uiCamera = OrthographicCamera()
    private val uiViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)
    private val renderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val padding = 20f
    private val layout = GlyphLayout()

    private val font = assetManager[AssetDescriptors.FONT]
    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]

    private val playerTexture = gameplayAtlas[RegionNames.PLAYER]
    private val obstacleTexture = gameplayAtlas[RegionNames.OBSTACLE]
    private val backgroundTexture = gameplayAtlas[RegionNames.BACKGROUND]


    private val stage: Stage = Stage(viewport)

    private val cardTexture = gameplayAtlas[RegionNames.CARD]

    // == public functions ==

    fun show() {
        stage.clear()

        val table = Table()
        for (j in 1..4) {
            table.row().pad(10f) // padding on all sides between cards
            for (k in 1..5) {
//                val cardDrawable = TextureRegionDrawable(TextureRegion(cardTexture))
//                val playButton = ImageButton(cardDrawable)
//                table.add(ImageTextButton("test", playButton))

//                val myCard = Image(cardTexture).apply {
//                    scaleX = 0.5f
//                    scaleY = 0.5f
//                }
                val myCard = Card("test-$j-$k", assetManager, myFont)
//                myCard.touchable = Touchable.enabled
                myCard.addListener(object : ClickListener() {
                    override fun touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                        log.debug("Touched $j-$k")

                        myCard.setCardText("Pushed")
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
        Gdx.input.inputProcessor = stage
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
        renderDebug(delta)
        renderUi(delta)

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

        viewport.drawGrid(renderer, (GameConfig.WORLD_WIDTH / 10).toInt())
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
        renderer.dispose()
        batch.dispose()
    }
}