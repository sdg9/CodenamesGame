package com.gofficer.codenames.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import java.awt.Color

fun clearScreen(color: Color = Color.BLACK) {
    clearScreen(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.alpha.toFloat());
}

fun clearScreen(red: Float, green: Float, blue: Float, alpha:Float) {
    Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
}