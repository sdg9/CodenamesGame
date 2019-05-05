package com.gofficer.codenames.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.game.CodenamesGame

fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.title = GameConfig.TITLE + " v" + GameConfig.VERSION
    config.width = GameConfig.WIDTH
    config.height = GameConfig.HEIGHT
    config.backgroundFPS = GameConfig.DEFAULT_FPS_LIMIT
    config.foregroundFPS = GameConfig.DEFAULT_FPS_LIMIT
//    config.resizable = false
    config.x = (1920 - GameConfig.WIDTH) / 2
    config.y = (1080 - GameConfig.HEIGHT) / 2
    LwjglApplication(CodenamesGame(), config)
}