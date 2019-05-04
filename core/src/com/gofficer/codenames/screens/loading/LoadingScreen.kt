package com.gofficer.codenames.screens.loading

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.game.CodenamesGame
import com.gofficer.codenames.screens.menu.MainMenuScreen
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.logger


class LoadingScreen(private val game: CodenamesGame) : ScreenAdapter() {

    companion object {
        @JvmStatic
        private val log = logger<LoadingScreen>()

        private const val PROGRESS_BAR_WIDTH = GameConfig.HUD_WIDTH / 2f // world units
        private const val PROGRESS_BAR_HEIGHT = 60f // world units

    }

    private val assetManager = game.assetManager
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var renderer: ShapeRenderer
    private var progress: Float = 0f

    override fun show() {
        log.debug("show")
        camera = OrthographicCamera()
        viewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, camera)
        renderer = ShapeRenderer()

        progress = 0f

        queueAssets()
    }

    override fun render(delta: Float) {

        clearScreen()

        // TODO I don't fully understand this
//        viewport.apply()
//        renderer.projectionMatrix = camera.combined

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
        renderer.dispose()
    }

    // == private functions ==
    private fun update(delta: Float) {
        progress = MathUtils.lerp(progress, assetManager.progress, .1f)
        if (assetManager.update() && progress >= assetManager.progress - .001f) {

            @Suppress("ConstantConditionIf")
            game.screen = if (GameConfig.USE_SPLASH) SplashScreen(game) else MainMenuScreen(game)
        }
    }

    private fun draw() {

        val progressBarX = (GameConfig.HUD_WIDTH - PROGRESS_BAR_WIDTH) / 2f
        val progressBarY = (GameConfig.HUD_HEIGHT - PROGRESS_BAR_HEIGHT) / 2f

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