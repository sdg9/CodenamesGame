package com.gofficer.sampler.samples

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.gofficer.sampler.common.SampleBase
import com.gofficer.sampler.utils.GdxArray
import com.gofficer.sampler.utils.clearScreen
import com.gofficer.sampler.utils.logger
import com.gofficer.sampler.utils.toInternalFile

/**
 * @author goran on 26/10/2017.
 */
class InputListeningSample : SampleBase() {

    companion object {
        @JvmStatic
        private val log = logger<InputListeningSample>()
    }

    lateinit var camera: OrthographicCamera
    lateinit var viewport: Viewport
    lateinit var batch: SpriteBatch
    lateinit var font: BitmapFont

    private val maxMessageCount = 15
    private val messages = GdxArray<String>()

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        log.debug("create()")

        camera = OrthographicCamera()
        viewport = FitViewport(1080f, 720f, camera)
        batch = SpriteBatch()
        font = BitmapFont("fonts/oswald-32.fnt".toInternalFile())

        Gdx.input.inputProcessor = this
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        // clear screen
        clearScreen()

        batch.projectionMatrix = camera.combined
        batch.begin()

        draw()

        batch.end()
    }

    private fun draw() {
        for (i in 0 until messages.size) {
            font.draw(batch, messages[i],
                    20f,
                    720 - 40f * i
            )
        }
    }

    private fun addMessage(message: String) {
        messages.add(message)

        if (messages.size > maxMessageCount) {
            messages.removeIndex(0)
        }
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val message = "touchUp screenX= $screenX, screenY= $screenY"
        log.debug(message)
        addMessage(message)
        return true
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val message = "mouseMoved screenX= $screenX, screenY= $screenY"
        log.debug(message)
        addMessage(message)
        return true
    }

    override fun keyTyped(character: Char): Boolean {
        val message = "keyTyped character= $character"
        log.debug(message)
        addMessage(message)
        return true

    }

    override fun scrolled(amount: Int): Boolean {
        val message = "scrolled amount= $amount"
        log.debug(message)
        addMessage(message)
        return true
    }

    override fun keyUp(keycode: Int): Boolean {
        val message = "keyUp keycode= $keycode"
        log.debug(message)
        addMessage(message)
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val message = "touchDragged screenX= $screenX, screenY= $screenY"
        log.debug(message)
        addMessage(message)
        return true
    }

    override fun keyDown(keycode: Int): Boolean {
        val message = "keyDown keycode= $keycode"
        log.debug(message)
        addMessage(message)
        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val message = "touchDown screenX= $screenX, screenY= $screenY"
        log.debug(message)
        addMessage(message)
        return true
    }
}