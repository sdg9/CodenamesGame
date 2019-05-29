package com.gofficer.codenames.screens.loading

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.CodenamesGame
import com.gofficer.codenames.GameClient
import com.gofficer.codenames.screens.menu.MainMenuScreen
import com.gofficer.codenames.screens.play.KeyCodeScreen
import com.gofficer.codenames.screens.play.PlayScreen
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.logger
import ktx.app.KtxScreen
import ktx.assets.disposeSafely


class LoadingScreen(private val client: GameClient) : KtxScreen {

    companion object {
        @JvmStatic
        private val log = logger<LoadingScreen>()

        private const val PROGRESS_BAR_WIDTH = GameConfig.HUD_WIDTH // world units

//        private const val PROGRESS_BAR_WIDTH = GameConfig.HUD_WIDTH / 2f // world units
        private const val PROGRESS_BAR_HEIGHT = 10f // world units

    }

    private val assetManager = client.assetManager
    private var camera: OrthographicCamera = OrthographicCamera()
    private var viewport: Viewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, camera)
    private var renderer: ShapeRenderer = ShapeRenderer()
    private var progress: Float = 0f

    override fun show() {
        log.debug("show")
//        camera = OrthographicCamera()
//        viewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, camera)
//        renderer = ShapeRenderer()
////        viewport.apply()
//
////        renderer.projectionMatrix = camera.combined

        progress = 0f

        queueAssets()
    }

    override fun render(delta: Float) {

        clearScreen()

        // TODO I don't fully understand this, required when changing between viewports
        viewport.apply()
        renderer.projectionMatrix = camera.combined



//        viewport.drawGrid(renderer)

        renderer.begin(ShapeRenderer.ShapeType.Filled)
        draw()
        renderer.end()


        update(delta)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun hide() {
        dispose()
    }

    override fun dispose() {
        log.debug("dispose")
        renderer.disposeSafely()
    }

    // == private functions ==
    private fun update(delta: Float) {
        progress = MathUtils.lerp(progress, assetManager.progress, .1f)
        if (assetManager.update() && progress >= assetManager.progress - .001f) {

            // Add all screens once assets loaded
//            client.addScreen(PlayScreen(client))
            client.addScreen(MainMenuScreen(client))
//            client.addScreen(SplashScreen(client))
//            client.addScreen(KeyCodeScreen(client))

//            @Suppress("ConstantConditionIf")
//            game.screen = if (GameConfig.USE_SPLASH) SplashScreen(game) else MainMenuScreen(game)
            client.setScreen<MainMenuScreen>()
//            game.screen = PlayScreen(game)
        }
    }

    private fun draw() {

        val progressBarX = (GameConfig.HUD_WIDTH - PROGRESS_BAR_WIDTH) / 2f
        val progressBarY = (GameConfig.HUD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2f


//        val progressBarX = 0f
//        val progressBarY = (GameConfig.HUD_HEIGHT) / 2f

        renderer.rect(progressBarX, progressBarY,
                progress * PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT)
    }

    private fun queueAssets() {
        assetManager.load(AssetDescriptors.FONT)
        assetManager.load(AssetDescriptors.GAMEPLAY)
        assetManager.load(AssetDescriptors.SPLASH)
        assetManager.load(AssetDescriptors.UI_SKIN)
    }
}