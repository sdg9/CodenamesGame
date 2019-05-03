package com.gofficer.sampler.common

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.InputProcessor

/**
 * @author goran on 29/10/2017.
 */
abstract class SampleBase : ApplicationAdapter(), InputProcessor {

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int) = false
    override fun mouseMoved(screenX: Int, screenY: Int) = false
    override fun keyTyped(character: Char) = false
    override fun scrolled(amount: Int) = false
    override fun keyUp(keycode: Int) = false
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int) = false
    override fun keyDown(keycode: Int) = false
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int) = false
}