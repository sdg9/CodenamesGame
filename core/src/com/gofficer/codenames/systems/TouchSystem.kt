package com.gofficer.codenames.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.gofficer.codenames.components.ClickableComponent
import com.gofficer.codenames.components.FlipAnimationComponent
import com.gofficer.codenames.components.RevealableComponent
import com.gofficer.codenames.components.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.log.info


class TouchSystem(private val camera: OrthographicCamera) : IteratingSystem(allOf(ClickableComponent::class, TransformComponent::class).get()) {

    private val transform  = mapperFor<TransformComponent>()
    private val clickable = mapperFor<ClickableComponent>()
    private val revealable = mapperFor<RevealableComponent>()

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val bounds = clickable[entity].bounds
        val position = transform[entity].position
        bounds.x = position.x
        bounds.y = position.y

        if (Gdx.input.justTouched()) {
//        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            // touched

            val clickPosition = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            info { "Clicked: $clickPosition, but bounds: $bounds" }
            if (bounds.contains(clickPosition.x, clickPosition.y)) {
                info { "Touched $entity"}
                revealable[entity]?.isRevealed = true
                entity?.add(FlipAnimationComponent())
            }
        }

    }
}