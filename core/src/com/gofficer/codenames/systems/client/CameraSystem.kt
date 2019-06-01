package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.config.GameConfig


class CameraSystem : BaseSystem() {

    var camera: OrthographicCamera = OrthographicCamera()
    var viewport: FitViewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    val uiCamera: OrthographicCamera = OrthographicCamera()
    val uiViewport: FitViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)

    init {
        camera.setToOrtho(false)
    }

    override fun processSystem() {
    }

}