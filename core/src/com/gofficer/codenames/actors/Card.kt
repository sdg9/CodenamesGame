package com.gofficer.codenames.actors

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.utils.get
import java.awt.SystemColor.text
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.gofficer.codenames.utils.logger


class Card(private var cardText: String, assetManager: AssetManager, private val font: BitmapFont) : Widget() {

    companion object {
        @JvmStatic
        private val log = logger<Card>()
    }
    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    private val cardTexture = gameplayAtlas[RegionNames.CARD]

     fun setCardText(text: String) {
        cardText = text
    }

    init {
        width = 260f / 1.5f
        height = 166f / 1.5f
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
//        super.draw(batch, parentAlpha)
        // draw image in center of actor

        font.color = Color.WHITE
//        font.setColor(0.2f, 0.5f, 0.2f, 1.0f);

        batch!!.draw(cardTexture, x, y, originX, originY,
                width, height, 1f, 1f, rotation)
        // draw text below image
        font.draw(batch, cardText, x + width / 7 , y + height / 2.5f)




//        var oldTransformMatrix = batch.transformMatrix.cpy();
//        val mx4Font = Matrix4()
//        val posX = 90f
//        val posY = -30f
//        mx4Font.translate(-posX, -posY, 0f);
//        mx4Font.rotate(0f, 0f, 1f, 25f);
//        mx4Font.translate(posX, posY, 0f);

//        mx4Font.rotate(Vector3(0f, 0f, 1f), 4f)
//        mx4Font.trn(5f, -19f, 0f)
//        batch.end()

//        mx4Font.setToRotation(Vector3(0f, 0f, 1f), 5f).translate(Vector3(x, y, 0f))
//        batch.transformMatrix = mx4Font
//        batch.begin()
//        font.draw(batch, cardText, x + 30 , y + 85)
//        batch.end()
//        batch.transformMatrix = oldTransformMatrix
//        batch.begin()

    }

    override fun getPrefWidth(): Float {
        return width
    }

    override fun getPrefHeight(): Float {
        return height
    }

}