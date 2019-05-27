package com.gofficer.codenames

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.components.*

fun dispatch(engine: Engine, action: Action) {
    val newEntity = engine.createEntity()
    newEntity.add(ActionComponent(action))
    engine.addEntity(newEntity)
}


fun createCard(engine: Engine, name: String, color: Color, x: Float, y: Float, id: Int, cardTexture: TextureRegion): Entity {
    val scaleFactor = 0.7f
    return engine.createEntity().apply {
        add(TextureComponent(cardTexture))
        add(TransformComponent(Vector2(x, y)))
        add(RevealableComponent())
        add(StateComponent())
        add(TeamComponent(color))
        add(NameComponent(name))
        add(IDComponent(id))
        add(
            RectangleComponent(
                cardTexture!!.regionWidth.toFloat() * scaleFactor,
                cardTexture!!.regionHeight.toFloat() * scaleFactor
            )
        )
        add(
            ClickableComponent(
                cardTexture!!.regionWidth.toFloat() * scaleFactor,
                cardTexture!!.regionHeight.toFloat() * scaleFactor
            )
        )
    }
}