package com.gofficer.codenames

import com.badlogic.gdx.InputProcessor
import com.gofficer.codenames.screens.play.PlayScreen
import ktx.log.logger

class MyInputProcessor : InputProcessor {

    companion object {
        @JvmStatic
        private val log = logger<MyInputProcessor>()
    }

    override fun keyDown(keycode: Int): Boolean {
        log.info { "keyDown" }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        log.info { "touchDown" }
        return true
    }

    override fun touchUp(x: Int, y: Int, pointer: Int, button: Int): Boolean {
        log.info { "touchUp" }
        return true
    }

    override fun touchDragged(x: Int, y: Int, pointer: Int): Boolean {
        return false
    }

    override fun mouseMoved(x: Int, y: Int): Boolean {
        log.info { "mouseMoved" }
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }
}