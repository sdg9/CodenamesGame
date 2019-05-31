package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
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
import com.badlogic.gdx.utils.viewport.Viewport
import ktx.log.logger

@Wire
@All(TextureRenderableComponent::class, PositionComponent::class)
class TouchSystem : BaseSystem() {

    val cameraSystem: CameraSystem? = null

    private val aimAtTmp = Vector3()


    companion object {
        val log = logger<TouchSystem>()
    }
    private lateinit var mPosition: ComponentMapper<PositionComponent>
    private val mRevealed by mapper<RevealedComponent>()

    override fun processSystem() {
//        val isTouched = Gdx.input.isTouched
        val justTouched = Gdx.input.justTouched()
        if (justTouched) {
            val x = Gdx.input.x.toFloat()
            val y = Gdx.input.y.toFloat()

            aimAtTmp.set(x, y, 0f)

            val unproject: Vector3 = cameraSystem!!.camera.unproject(aimAtTmp)

            log.debug{ "Screen space touched $x, $y"}
//            val clickPosition = cameraSystem!!.camera.unproject(Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            log.debug{ "World space touched ${unproject.x}, ${unproject.y}"}
        }
    }
}
