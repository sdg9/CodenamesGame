package com.gofficer.codenames.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.viewport.FitViewport

import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.gofficer.codenames.game.Application

class SplashScreen(private val app: Application) : Screen {
    private val stage: Stage

    private var splashImg: Image? = null

    init {
        this.stage = Stage(FitViewport(Application.V_WIDTH, Application.V_HEIGHT, app.camera))
    }

    override fun show() {
        println("SPLASH")
        Gdx.input.inputProcessor = stage

        val transitionRunnable = Runnable { app.setScreen(app.mainMenuScreen) }

//        val splashTex = app.assets.get("img/splash.png", Texture::class.java)
//        app.assets.load()
        val splashTex = app.assets.get("img/splash.png", Texture::class.java)
        splashImg = Image(splashTex)
        splashImg!!.setOrigin(splashImg!!.width / 2, splashImg!!.height / 2)
        splashImg!!.setPosition(stage.width / 2 - 32, stage.height + 32)
        splashImg!!.addAction(sequence(alpha(0f), scaleTo(.1f, .1f),
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

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {
        stage.dispose()
    }
}