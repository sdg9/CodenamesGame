package com.gofficer.sampler.samples

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.gofficer.sampler.common.SampleBase
import com.gofficer.sampler.utils.clearScreen
import com.gofficer.sampler.utils.isKeyPressed
import com.gofficer.sampler.utils.logger
import com.gofficer.sampler.utils.toInternalFile

/**
 * @author goran on 26/10/2017.
 */
class OrthographicCameraSample : SampleBase() {

    companion object {
        @JvmStatic
        private val log = logger<OrthographicCameraSample>()

        private const val WORLD_WIDTH = 10.8f // world units
        private const val WORLD_HEIGHT = 7.2f // world units
        private const val CAMERA_SPEED = 2.0f // world units
        private const val CAMERA_ZOOM_SPEED = 2.0f // world units
    }


    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var batch: SpriteBatch
    private lateinit var texture: Texture


    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG

        log.debug("create()")

        camera = OrthographicCamera()
//        camera.setToOrtho(true)
        viewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)
        batch = SpriteBatch()
        texture = Texture("raw/level-bg.png".toInternalFile())
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        clearScreen()
        queryInput()

        batch.projectionMatrix = camera.combined
        batch.begin()

        draw()

        batch.end()
    }

    private fun queryInput() {
        val deltaTime = Gdx.graphics.deltaTime


        when {
            Input.Keys.LEFT.isKeyPressed() -> camera.position.x -= CAMERA_SPEED * deltaTime
            Input.Keys.RIGHT.isKeyPressed() -> camera.position.x += CAMERA_SPEED * deltaTime
            Input.Keys.UP.isKeyPressed() -> camera.position.y += CAMERA_SPEED * deltaTime
            Input.Keys.DOWN.isKeyPressed() -> camera.position.y -= CAMERA_SPEED * deltaTime
            Input.Keys.PAGE_UP.isKeyPressed() -> camera.zoom -= CAMERA_ZOOM_SPEED * deltaTime
            Input.Keys.PAGE_DOWN.isKeyPressed() -> camera.zoom += CAMERA_ZOOM_SPEED * deltaTime
            Input.Keys.ENTER.isKeyPressed() -> {
                log.debug("position= ${camera.position}")
                log.debug("zoom= ${camera.zoom}")
            }
//            else -> log.debug("no keys pressed")
        }

        camera.update()
    }

    private fun draw() {
        batch.draw(texture, 0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
    }

    override fun dispose() {
        log.debug("dispose")
        batch.dispose()
        texture.dispose()
    }
}