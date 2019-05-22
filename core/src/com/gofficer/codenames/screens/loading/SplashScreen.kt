package com.gofficer.codenames.screens.loading

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.FitViewport

import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.CodenamesGame
import com.gofficer.codenames.screens.menu.MainMenuScreen
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.utils.logger
import ktx.app.KtxScreen

class SplashScreen(private val game: CodenamesGame) : KtxScreen {

    companion object {
        @JvmStatic
        private val log = logger<SplashScreen>()
    }


    private val camera = OrthographicCamera()
    private val stage: Stage = Stage(FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera))
    private val splashAtlas = game.assetManager[AssetDescriptors.SPLASH]
    private val splashIconTexture = splashAtlas[RegionNames.SPLASH_ICON]
    private var splashImg: Image = Image(splashIconTexture)

    override fun show() {
        log.debug("show")
        Gdx.input.inputProcessor = stage

        val transitionRunnable = Runnable {
//            game.screen = MainMenuScreen(game)
            game.setScreen<MainMenuScreen>()
        }

//        val splashTex = game.assets.get("img/splash.png", Texture::class.java)
//        game.assets.load()
//        val splashTex = game.assetManager.get("img/splash.png", Texture::class.java)
        splashImg.setOrigin(splashImg.width / 2, splashImg.height / 2)
        splashImg.setPosition(stage.width / 2 - 32, stage.height + 32)
        splashImg.addAction(sequence(alpha(0f), scaleTo(.1f, .1f),
                parallel(fadeIn(2f, Interpolation.pow2),
                        scaleTo(2f, 2f, 2.5f, Interpolation.pow5),
                        moveTo(stage.width / 2 - 32, stage.height / 2 - 32, 2f, Interpolation.swing)),
                delay(1.5f), fadeOut(1.25f), run(transitionRunnable)))

        stage.addActor(splashImg)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        update(delta)

        stage.draw()
    }

    fun update(delta: Float) {
        stage.act(delta)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, false)
    }

    override fun dispose() {
        stage.dispose()
    }
}