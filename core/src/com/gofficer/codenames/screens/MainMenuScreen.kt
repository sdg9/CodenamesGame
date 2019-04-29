package com.gofficer.codenames.screens


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport

import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.gofficer.codenames.game.Application

class MainMenuScreen(private val app: Application) : Screen {

    private val stage: Stage
    private var skin: Skin? = null

    private var buttonPlay: TextButton? = null
    private var buttonExit: TextButton? = null

    private val shapeRenderer: ShapeRenderer

    init {
        this.stage = Stage(FitViewport(Application.V_WIDTH, Application.V_HEIGHT, app.camera))
        this.shapeRenderer = ShapeRenderer()
    }

    override fun show() {
        println("MAIN MENU")
        Gdx.input.inputProcessor = stage
        stage.clear()

        this.skin = Skin()
        this.skin?.addRegions(app.assets?.get("ui/uiskin.atlas", TextureAtlas::class.java))
        this.skin?.add("default-font", app.font24)
        this.skin?.load(Gdx.files.internal("ui/uiskin.json"))

        initButtons()
    }

    private fun update(delta: Float) {
        stage.act(delta)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        update(delta)

        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {
        stage.dispose()
        shapeRenderer.dispose()
    }

    private fun initButtons() {
        buttonPlay = TextButton("Play", skin!!, "default")
        buttonPlay!!.setPosition(110f, 260f)
        buttonPlay!!.setSize(280f, 60f)
        buttonPlay!!.addAction(sequence(alpha(0f), parallel(fadeIn(.5f), moveBy(0f, -20f, .5f, Interpolation.pow5Out))))
        buttonPlay!!.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                app.screen = app.playScreen
            }
        })

        buttonExit = TextButton("Exit", skin!!, "default")
        buttonExit?.setPosition(110f, 190f)
        buttonExit?.setSize(280f, 60f)
        buttonExit?.addAction(sequence(alpha(0f), parallel(fadeIn(.5f), moveBy(0f, -20f, .5f, Interpolation.pow5Out))))
        buttonExit?.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })

        stage.addActor(buttonPlay)
        stage.addActor(buttonExit)
    }
}