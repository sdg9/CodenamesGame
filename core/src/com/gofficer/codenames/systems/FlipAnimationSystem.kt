package com.gofficer.codenames.systems

import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.gofficer.codenames.components.*
import com.gofficer.codenames.utils.require
import ktx.log.logger

//
//import com.badlogic.ashley.core.Entity
//import com.badlogic.ashley.core.Family
//import com.badlogic.ashley.systems.IteratingSystem
//import com.badlogic.gdx.math.Vector2
//import com.gofficer.codenames.components.*
//import ktx.ashley.allOf
//import ktx.ashley.mapperFor
//import ktx.ashley.remove
//import ktx.log.info
//
//val flippableFamily: Family = allOf(FlipAnimationComponent::class, RectangleComponent::class).get()
//
@All(FlipAnimationComponent::class)
class FlipAnimationSystem: IteratingSystem() {

    companion object {
        val log = logger<FlipAnimationSystem>()
    }

    private val mCard by require<CardComponent>()
    private val mTransform by require<TransformComponent>()
    private val mPosition by require<PositionComponent>()
    private val mFlipAnimation by require<FlipAnimationComponent>()
    private val mRectangle by require<RectangleComponent>()

    private val animationDuration = 0.45f


    override fun process(entityId: Int) {
        val position = mPosition.get(entityId)
        val myRectangle = mRectangle.get(entityId)
        val myFlipAnimation = mFlipAnimation.get(entityId)

        // TODO: How Do i properly get time delta?
        myFlipAnimation.time += world.getDelta()

        log.info { "Processsing flip animation ${myFlipAnimation.time}"}

        if (!myFlipAnimation.hasStarted) {
            myFlipAnimation.hasStarted = true
            myFlipAnimation.initialX = position.x
            myFlipAnimation.initialWidth = myRectangle.width
            log.info { "Initial width: ${myRectangle.width}"}
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
            log.info { "Removing animation"}
//            myFlipAnimation.time = 0f
            position.x = myFlipAnimation.initialX
            myRectangle.width = myFlipAnimation.initialWidth

            mFlipAnimation.remove(entityId)
//            entity?.remove<FlipAnimationComponent>()
        }
    }

    private fun getXForFlip(flipAnimationComponent: FlipAnimationComponent, rectangle: RectangleComponent): Float {
        return flipAnimationComponent.initialX + ((flipAnimationComponent.initialWidth / 2) - (rectangle.width / 2))
    }

}