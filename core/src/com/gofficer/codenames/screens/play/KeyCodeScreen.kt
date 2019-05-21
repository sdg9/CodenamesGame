package com.gofficer.codenames.screens.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.gofficer.codenames.CodenamesGame
import com.gofficer.codenames.utils.logger

class KeyCodeScreen(val game: CodenamesGame) : ScreenAdapter() {

    companion object {
        @JvmStatic
        private val log = logger<KeyCodeScreen>()
    }

    private val assetManager = game.assetManager
    private lateinit var renderer: KeyCodeRenderer

    override fun show() {
        log.debug("show")
        renderer = KeyCodeRenderer(game.font24, assetManager, game.store)
        renderer.show()
    }

    override fun render(delta: Float) {
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