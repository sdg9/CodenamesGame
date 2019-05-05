package com.gofficer.codenames.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle

@JvmOverloads
fun clearScreen(color: Color = Color.BLACK) = clearScreen(color.r, color.g, color.b, color.a)

fun clearScreen(red: Float, green: Float, blue: Float, alpha: Float) {
    // clear screen
    // DRY - Don't repeat yourself
    // WET - Waste everyone's time
//    Gdx.gl.glClearColor(red, green, blue, alpha)
//    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    Gdx.gl.glClearColor(.135f, .206f, .235f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
}

inline fun Batch.use(action: () -> Unit) {
    begin()
    action()
    end()
}

inline fun ShapeRenderer.use(action: () -> Unit) {
    begin(ShapeRenderer.ShapeType.Line)
    action()
    end()
}

@JvmOverloads
fun ShapeRenderer.circle(c: Circle, segments: Int = 30) {
    circle(c.x, c.y, c.radius, segments)
}