package com.gofficer.codenames.systems.client

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.PositionComponent
import com.gofficer.codenames.components.RevealedComponent
import com.gofficer.codenames.components.TextureRenderableComponent
import com.gofficer.codenames.utils.RenderSystemMarker
import com.gofficer.codenames.utils.mapper

//
//import com.badlogic.ashley.core.Entity
//import com.badlogic.ashley.systems.IteratingSystem
//import com.badlogic.gdx.Gdx
//import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import ktx.log.logger

//import com.badlogic.gdx.math.Vector3
//import com.gofficer.codenames.components.*
////import com.gofficer.codenames.dispatch
//import ktx.ashley.allOf
//import ktx.ashley.mapperFor
//import ktx.log.info
//
@Wire
@All(TextureRenderableComponent::class, PositionComponent::class)
class TouchSystem(private val gameWorld: GameWorld, private val camera: Camera) : IteratingSystem() ,
    RenderSystemMarker {

    companion object {
        val log = logger<TouchSystem>()
    }
    private lateinit var mPosition: ComponentMapper<PositionComponent>
    private val mRevealed by mapper<RevealedComponent>()

    override fun process(entityId: Int) {

        val isRevealed = mRevealed.has(entityId)
        val position = mPosition.get(entityId)
        // TODO make dynamic
        val bounds = Rectangle(position.x, position.y, 150f, 100f)

        if (Gdx.input.justTouched()) {
            val clickPosition = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
//            val clickPosition = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            val clickedX = clickPosition.x
            val clickedY = clickPosition.y
//            val clickedX = Gdx.input.x.toFloat()
//            val clickedY = Gdx.input.y.toFloat()

            log.info { "Touch detected $clickedX, $clickedY"}
            if (bounds.contains(clickedX, clickedY)) {
                log.info { "Touched entity $entityId"}
                if (!isRevealed) {
                    //TODO
                    log.info { "Tell server entity is touched"}
                }
            }
        }
        //        val myBounds = Mappers.clickable[entity]
//        val position = Mappers.transform[entity].position
////        val car
//
//        val bounds = Rectangle(position.x, position.y, myBounds.width, myBounds.height)
////        bounds.x = position.x
////        bounds.y = position.y
////        info { "Clicked at x: $" }
//
//        if (Gdx.input.justTouched()) {
////        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
//            // touched
//
//            val clickPosition = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
////            info { "Clicked x: ${Gdx.input.x.toFloat()}, y: ${Gdx.input.y.toFloat()}" }
////            info { "Clicked: $clickPosition, but bounds: $bounds" }
//
//            val clickedX = clickPosition.x
//            val clickedY = clickPosition.y
////            val clickedX = Gdx.input.x.toFloat()
////            val clickedY = Gdx.input.y.toFloat()
////            info { "Clicked $clickedX, $clickedY"}
//
////            if (bounds.contains(clickPosition.x, clickPosition.y)) {
//            if (bounds.contains(clickedX, clickedY)) {
//                info { "Touched $entity"}
//
//                // Only apply if not already revealed
//                if (Mappers.revealable[entity]?.isRevealed != true) {
//                    // TODO if going pure dispatch, then biz logic should reside in dispatch system not here this just fires event
//                    Mappers.revealable[entity]?.isRevealed = true
//                    entity?.add(FlipAnimationComponent())
//                    entity?.add(NetworkComponent())
////                    entity?.add(RemoveComponent())
//
//                    // TODO give real id
//                    dispatch(engine, TouchCardAction(12))
//                }
//            }
//        }
//
    }


}
//
//class TouchSystem(private val camera: OrthographicCamera) : IteratingSystem(allOf(ClickableComponent::class, TransformComponent::class).get()) {
//
////    private val rectangleMapper = mapperFor<RectangleComponent>()
//
//    override fun processEntity(entity: Entity?, deltaTime: Float) {
//        val myBounds = Mappers.clickable[entity]
//        val position = Mappers.transform[entity].position
////        val car
//
//        val bounds = Rectangle(position.x, position.y, myBounds.width, myBounds.height)
////        bounds.x = position.x
////        bounds.y = position.y
////        info { "Clicked at x: $" }
//
//        if (Gdx.input.justTouched()) {
////        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
//            // touched
//
//            val clickPosition = camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
////            info { "Clicked x: ${Gdx.input.x.toFloat()}, y: ${Gdx.input.y.toFloat()}" }
////            info { "Clicked: $clickPosition, but bounds: $bounds" }
//
//            val clickedX = clickPosition.x
//            val clickedY = clickPosition.y
////            val clickedX = Gdx.input.x.toFloat()
////            val clickedY = Gdx.input.y.toFloat()
////            info { "Clicked $clickedX, $clickedY"}
//
////            if (bounds.contains(clickPosition.x, clickPosition.y)) {
//            if (bounds.contains(clickedX, clickedY)) {
//                info { "Touched $entity"}
//
//                // Only apply if not already revealed
//                if (Mappers.revealable[entity]?.isRevealed != true) {
//                    // TODO if going pure dispatch, then biz logic should reside in dispatch system not here this just fires event
//                    Mappers.revealable[entity]?.isRevealed = true
//                    entity?.add(FlipAnimationComponent())
//                    entity?.add(NetworkComponent())
////                    entity?.add(RemoveComponent())
//
//                    // TODO give real id
//                    dispatch(engine, TouchCardAction(12))
//                }
//            }
//        }
//
//    }
//}