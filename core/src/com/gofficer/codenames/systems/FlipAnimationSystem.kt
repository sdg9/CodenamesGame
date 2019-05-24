package com.gofficer.codenames.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.components.*
import ktx.ashley.allOf
import ktx.ashley.mapperFor
import ktx.ashley.remove
import ktx.log.info

val flippableFamily: Family = allOf(FlipAnimationComponent::class, RectangleComponent::class).get()

class FlipAnimationSystem : IteratingSystem(flippableFamily) {

    private val animationDuration = 0.45f

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        val position = Mappers.transform[entity].position
        val myRectangle = Mappers.rectangle[entity]
        val myFlipAnimation = Mappers.flipAnimation[entity]

        myFlipAnimation.time += deltaTime

        if (!myFlipAnimation.hasStarted) {
            myFlipAnimation.hasStarted = true
            myFlipAnimation.initialX = position.x
            myFlipAnimation.initialWidth = myRectangle.width
            info { "Initial width: ${myRectangle.width}"}
        }

        val totalTimeRemaining = animationDuration - myFlipAnimation.time

        val isFirstHalf = totalTimeRemaining > animationDuration / 2
        val isSecondHalf = totalTimeRemaining > 0f

        if (isFirstHalf) {
            val flipAwayCompletion = myFlipAnimation.time / (animationDuration / 2)
            // Decrease width to 0 as flip completion increases
            myRectangle.width = myFlipAnimation.initialWidth * (1 - flipAwayCompletion)
            position.x = getXForFlip(myFlipAnimation, myRectangle)
        } else if (isSecondHalf) {
            myFlipAnimation.suppressColor = false
            val flipBackCompletion = (myFlipAnimation.time - (animationDuration / 2)) / (animationDuration / 2)
            // Increase width to initialWidth as flip completion increases
            myRectangle.width = myFlipAnimation.initialWidth * flipBackCompletion
            position.x = getXForFlip(myFlipAnimation, myRectangle)
        } else {
            info { "Removing animation"}
//            myFlipAnimation.time = 0f
            position.x = myFlipAnimation.initialX
            myRectangle.width = myFlipAnimation.initialWidth
            entity?.remove<FlipAnimationComponent>()
        }
    }

    private fun getXForFlip(flipAnimationComponent: FlipAnimationComponent, rectangle: RectangleComponent): Float {
        return flipAnimationComponent.initialX + ((flipAnimationComponent.initialWidth / 2) - (rectangle.width / 2))
    }

}