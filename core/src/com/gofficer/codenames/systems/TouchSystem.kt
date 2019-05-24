package com.gofficer.codenames.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.gofficer.codenames.components.*
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.log.info



class TouchSystem(private val camera: OrthographicCamera) : IteratingSystem(allOf(ClickableComponent::class, TransformComponent::class).get()) {

    private val transform  = mapperFor<TransformComponent>()
    private val clickable = mapperFor<ClickableComponent>()
    private val revealable = mapperFor<RevealableComponent>()
//    private val rectangleMapper = mapperFor<RectangleComponent>()

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val myBounds = clickable[entity]
//        val rectangle = rectangleMapper[entity]
        val position = transform[entity].position

        val bounds = Rectangle(position.x, position.y, myBounds.width, myBounds.height)
//        bounds.x = position.x
//        bounds.y = position.y
//        info { "Clicked at x: $" }

        if (Gdx.input.justTouched()) {
//        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            // touched

            val clickPosition = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
//            info { "Clicked x: ${Gdx.input.x.toFloat()}, y: ${Gdx.input.y.toFloat()}" }
//            info { "Clicked: $clickPosition, but bounds: $bounds" }

            val clickedX = clickPosition.x
            val clickedY = clickPosition.y
//            val clickedX = Gdx.input.x.toFloat()
//            val clickedY = Gdx.input.y.toFloat()
            info { "Clicked $clickedX, $clickedY"}

//            if (bounds.contains(clickPosition.x, clickPosition.y)) {
            if (bounds.contains(clickedX, clickedY)) {
                info { "Touched $entity"}

                // Only apply if not already revealed
                if (revealable[entity]?.isRevealed != true) {
                    revealable[entity]?.isRevealed = true
                    entity?.add(FlipAnimationComponent())
                }
            }
        }

    }
}