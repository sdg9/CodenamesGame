package com.gofficer.sampler.samples

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gofficer.sampler.common.SampleBase

/**
 * @author goran on 26/10/2017.
 */
class GdxGeneratedSample : SampleBase() {

    lateinit var batch: SpriteBatch
    lateinit var img: Texture

    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
    }

    override fun render() {
        // clear screen
        // DRY - Don't repeat yourself
        // WET - Waste everyone's time
        Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        batch.draw(img, 0.0f, 0.0f)
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}