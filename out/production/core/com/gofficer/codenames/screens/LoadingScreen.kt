package com.gofficer.codenames.screens

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.gofficer.codenames.game.Application


class LoadingScreen(private val app: Application) : Screen {

    private val shapeRenderer: ShapeRenderer
    private var progress: Float = 0.toFloat()

    init {
        this.shapeRenderer = ShapeRenderer()
    }

    private fun queueAssets() {
        app.assets.load("img/splash.png", Texture::class.java)
        app.assets.load("ui/uiskin.atlas", TextureAtlas::class.java)
    }

    override fun show() {
        println("LOADING")
        shapeRenderer.projectionMatrix = app.camera.combined
        this.progress = 0f
        queueAssets()
    }

    private fun update(delta: Float) {
        progress = MathUtils.lerp(progress, app.assets.progress, .1f)
        if (app.assets.update() && progress >= app.assets.progress - .001f) {
//            app.screen = app.splashScreen
            app.screen = app.mainMenuScreen
        }
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        update(delta)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color.BLACK
        shapeRenderer.rect(32f, app.camera.viewportHeight / 2 - 8, app.camera.viewportWidth - 64, 16f)

        shapeRenderer.color = Color.BLUE
        shapeRenderer.rect(32f, app.camera.viewportHeight / 2 - 8, progress * (app.camera.viewportWidth - 64), 16f)
        shapeRenderer.end()
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {
        shapeRenderer.dispose()
    }
}