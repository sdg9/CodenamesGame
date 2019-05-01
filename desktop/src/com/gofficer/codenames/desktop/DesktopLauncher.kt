package com.gofficer.codenames.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.gofficer.codenames.game.Application

fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration()
    config.title = Application.TITLE + " v" + Application.VERSION
    config.width = Application.V_WIDTH.toInt()
    config.height = Application.V_HEIGHT.toInt()
    config.backgroundFPS = 60
    config.foregroundFPS = 60
    config.resizable = false
    LwjglApplication(Application(), config)
}