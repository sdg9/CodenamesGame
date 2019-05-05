package com.gofficer.codenames.screens.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.drawGrid
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.utils.logger

class PlayRenderer(private val assetManager: AssetManager,
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

    // == public functions ==
    fun render() {
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

        renderGamePlay()
        renderDebug()
        renderUi()

//        log.debug("totalRenderCalls= ${batch.totalRenderCalls}")
    }

    private fun renderGamePlay() {
        viewport.apply()
        batch.projectionMatrix = camera.combined
    }

    private fun renderDebug() {
        viewport.apply()
        batch.projectionMatrix = camera.combined

        viewport.drawGrid(renderer, (GameConfig.WORLD_WIDTH / 10).toInt())
    }

    private fun renderUi() {
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