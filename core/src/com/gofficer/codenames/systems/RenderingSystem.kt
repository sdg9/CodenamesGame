package com.gofficer.codenames.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gofficer.codenames.components.*
import com.gofficer.codenames.utils.use
import ktx.ashley.allOf
import ktx.ashley.has
import ktx.ashley.mapperFor


class RenderingSystem(val batch: SpriteBatch, val font: BitmapFont) : IteratingSystem(allOf(
    TransformComponent::class,
    TextureComponent::class,
    RectangleComponent::class,
    NameComponent::class,
    TeamComponent::class
).get()) {

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val img = Mappers.texture[entity].texture
        val position = Mappers.transform[entity].position
        val isRevealed = Mappers.revealable[entity]?.isRevealed
        val myRectangle = Mappers.rectangle[entity]
        val myName = Mappers.name[entity].name
        val teamColor = Mappers.teamMapper[entity].teamColor
        val isAnimating: Boolean = entity?.has(Mappers.animation) ?: false
        val suppressColor = Mappers.animation[entity]?.suppressColor == true

        val layout = GlyphLayout(font, myName)
        val fontX = position.x + (myRectangle.width - layout.width) / 2
        val fontY = position.y + (myRectangle.height - layout.height) / 3
        font.color = Color.BLACK

        batch.use {
            batch.color = if (isRevealed == true && !suppressColor) teamColor else Color.WHITE
            batch.draw(img, position.x, position.y, myRectangle.width, myRectangle.height)
            if (!isAnimating) {
                font.draw(batch, myName, fontX, fontY)
            }
            batch.color = Color.WHITE
        }

    }
}