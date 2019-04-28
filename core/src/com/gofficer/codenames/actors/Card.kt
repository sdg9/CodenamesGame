package com.gofficer.codenames.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.scenes.scene2d.Actor


class Card(x: Float, y: Float, width: Float, height: Float, color: Color) : Actor() {

    private var texture: Texture? = null

    init {
        createTexture(width.toInt(), height.toInt(), color)

        setX(x)
        setY(y)
        setWidth(width)
        setHeight(height)
    }

    private fun createTexture(width: Int, height: Int, color: Color) {
        val pixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fillRectangle(0, 0, width, height)
        texture = Texture(pixmap)
        pixmap.dispose()
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        val color = color
        batch!!.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        batch.draw(texture, x, y, width, height)
    }
}