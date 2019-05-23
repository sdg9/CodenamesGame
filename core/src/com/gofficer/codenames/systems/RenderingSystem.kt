package com.gofficer.codenames.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gofficer.codenames.components.RectangleComponent
import com.gofficer.codenames.components.RevealableComponent
import com.gofficer.codenames.components.TextureComponent
import com.gofficer.codenames.components.TransformComponent
import com.gofficer.codenames.utils.use
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import java.util.concurrent.RecursiveAction


class RenderingSystem(val batch: SpriteBatch) : IteratingSystem(allOf(
    TransformComponent::class,
    TextureComponent::class,
    RectangleComponent::class
).get()) {

    private val transform  = mapperFor<TransformComponent>()
    private val texture = mapperFor<TextureComponent>()
    private val revealable = mapperFor<RevealableComponent>()
    private val rectangle = mapperFor<RectangleComponent>()

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val img = texture[entity].texture
        val position = transform[entity].position
        val isRevealed = revealable[entity]?.isRevealed
        val myRectangle = rectangle[entity]
//        info { "Entity being processed $entity $position"}

//        val width: Float = img?.regionWidth?.toFloat() ?: 0f
//        val width = 12f
        val height: Float = img?.regionHeight?.toFloat() ?: 0f
        batch.use {
            batch.color = if (isRevealed == true) Color.BLUE else Color.WHITE
            batch.draw(img, position.x, position.y, myRectangle.width, myRectangle.height)
            batch.color = Color.WHITE
        }

    }
}