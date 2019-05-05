package com.gofficer.codenames.screens.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.ScreenAdapter
import com.gofficer.codenames.game.CodenamesGame
import com.gofficer.codenames.utils.logger

class PlayScreen(val game: CodenamesGame) : ScreenAdapter() {

    companion object {
        @JvmStatic
        private val log = logger<PlayScreen>()
    }

    private val assetManager = game.assetManager
    private lateinit var controller: PlayController
    private lateinit var renderer: PlayRenderer


    override fun show() {
        log.debug("show")
        controller = PlayController()
        renderer = PlayRenderer(game.font24, assetManager, controller, game.store)
        renderer.show()
    }

    override fun render(delta: Float) {
        controller.update(delta)
        renderer.render(delta)


        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            hide()
            show()
        }
    }

    override fun resize(width: Int, height: Int) {
        log.debug("resize")
        renderer.resize(width, height)
    }

    override fun hide() {
        log.debug("hide")
        dispose()
    }

    override fun dispose() {
        log.debug("dispose")
        renderer.dispose()
    }
}