package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.badlogic.gdx.Gdx
import com.gofficer.codenames.components.PositionComponent
import com.gofficer.codenames.components.RevealedComponent
import com.gofficer.codenames.components.TextureRenderableComponent
import net.mostlyoriginal.api.event.common.EventSystem
import com.gofficer.codenames.utils.mapper
import com.badlogic.gdx.math.Vector3
import com.gofficer.codenames.event.TouchEvent
import ktx.log.logger



@Wire
class TouchSystem : BaseSystem() {

    private val cameraSystem: CameraSystem? = null

    private val aimAtTmp = Vector3()

    var eventSystem: EventSystem? = null

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
            eventSystem?.dispatch(TouchEvent(unproject.x, unproject.y))
        }
    }
}
