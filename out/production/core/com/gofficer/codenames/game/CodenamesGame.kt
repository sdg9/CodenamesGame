package com.gofficer.codenames.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gofficer.codenames.redux.Dispatch
import com.gofficer.codenames.redux.Unsubscribe
import gofficer.codenames.game.GameState


class CodenamesGame : ApplicationAdapter() {
    internal lateinit var batch: SpriteBatch
    internal lateinit var img: Texture
    internal var red: Float = 0f
    internal var green: Float = 0f
    internal var blue: Float = 0f


    internal lateinit var store: Gamestore
    private var unsubscribe: Unsubscribe? = null
    private var dispatch: Dispatch? = null
    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")


        val initState = GameState(0f, 0f, 0f, 1, false)
        store = Gamestore(initState)
        unsubscribe = store.subscribe({ state, dispatch ->
            this.dispatch = dispatch

            Gdx.app.log("Subscribe", "State updated " + state.toString());
            red = state.red
            blue = state.blue
            green = state.green
        })


        Gdx.input.inputProcessor = object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                if (button == Input.Buttons.LEFT) {
                    // do something
                    Gdx.app.log("ApplicationAdapter", "Some Log")
                    var rand1 = Math.round( Math.random() ).toFloat()
                    var rand2 = Math.round( Math.random() ).toFloat()
                    var rand3 = Math.round( Math.random() ).toFloat()
                    store.dispatch(ChangeColor(rand1, rand2, rand3))
                }
                return false
            }
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(red, blue, green, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)
        batch.end()


        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            dispose()
            create()
        }
    }

    override fun dispose() {
        batch.dispose()
        img.dispose()
    }
}
