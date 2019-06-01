package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.artemis.annotations.Wire
import com.badlogic.gdx.Gdx
import net.mostlyoriginal.api.event.common.EventSystem
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

    override fun processSystem() {
//        val isTouched = Gdx.input.isTouched
        val justTouched = Gdx.input.justTouched()
        if (justTouched) {
            val x = Gdx.input.x.toFloat()
            val y = Gdx.input.y.toFloat()

            aimAtTmp.set(x, y, 0f)

            val unproject: Vector3 = cameraSystem!!.camera.unproject(aimAtTmp)

            log.debug{ "Screen space touched $x, $y"}
            log.debug{ "World space touched ${unproject.x}, ${unproject.y}"}
            eventSystem?.dispatch(TouchEvent(unproject.x, unproject.y))
        }
    }
}
