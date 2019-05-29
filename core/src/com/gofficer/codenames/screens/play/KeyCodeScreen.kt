package com.gofficer.codenames.screens.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.gofficer.codenames.GameClient
import com.gofficer.codenames.utils.logger
import ktx.app.KtxScreen

class KeyCodeScreen(val game: GameClient) : KtxScreen {

    companion object {
        @JvmStatic
        private val log = logger<KeyCodeScreen>()
    }

    private val assetManager = game.assetManager
//    private var renderer: KeyCodeRenderer = KeyCodeRenderer(game.font24, assetManager, game.store)

    override fun show() {
        log.debug("show")
//
//        if (game.client == null) {
//            setupGame()
//        }
//        game.store.subscribe {
//            log.debug("Update to store")
//        }
//        renderer.show()
    }

    override fun render(delta: Float) {
//        controller.update(delta)
//        renderer.render(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            hide()
            show()
        }
    }

    override fun resize(width: Int, height: Int) {
        log.debug("resize")
//        renderer.resize(width, height)
    }

    override fun hide() {
        log.debug("hide")
        dispose()
    }

    override fun dispose() {
        log.debug("dispose")
//        renderer.dispose()
    }

    private fun setupGame() {
//        game.store.dispatch(SetupGame())
    }
}