package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.artemis.annotations.Wire
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import ktx.log.logger

@Wire
class KeyboardInputSystem() : BaseSystem() {

    companion object {
        val log = logger<MouseCursorSystem>()
    }

    override fun processSystem() {

        val isPressed = Gdx.input.isKeyPressed(Input.Keys.A)
        if (isPressed) {
            log.debug{ "A is pressed"}
        }
    }

}