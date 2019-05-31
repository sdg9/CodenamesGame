package com.gofficer.codenames.systems.client

import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.Gdx
import com.artemis.Aspect
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Input
import com.gofficer.codenames.components.MouseCursorComponent
import com.gofficer.codenames.components.PositionComponent
import com.gofficer.codenames.utils.mapper
import ktx.log.logger


@Wire
class MouseCursorSystem : IteratingSystem(Aspect.all(PositionComponent::class.java, MouseCursorComponent::class.java)) {

    companion object {
        val log = logger<MouseCursorSystem>()
    }

    private val mPosition by mapper<PositionComponent>()
    private val mMouseCursor by mapper<MouseCursorComponent>()

    private val cameraSystem: CameraSystem? = null

    private val aimAtTmp = Vector3()

    override fun process(e: Int) {

        val pos = mPosition.get(e)

        aimAtTmp.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)

        val unproject = cameraSystem!!.camera.unproject(aimAtTmp)

        log.debug{ "Found ${unproject.x}, ${unproject.y}"}

        pos.x = unproject.x
        pos.y = unproject.y
    }
}