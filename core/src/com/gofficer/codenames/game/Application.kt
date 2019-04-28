package com.gofficer.codenames.game

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gofficer.codenames.components.Card
import com.gofficer.codenames.redux.Dispatch
import com.gofficer.codenames.redux.Unsubscribe
import gofficer.codenames.game.GameState


class Application : Game() {
    internal lateinit var batch: SpriteBatch
    internal lateinit var img: Texture
    internal var red: Float = 0f
    internal var green: Float = 0f
    internal var blue: Float = 0f
    lateinit var card: Card
    lateinit var camera: OrthographicCamera


    internal lateinit var store: Gamestore
    private var unsubscribe: Unsubscribe? = null
    private var dispatch: Dispatch? = null
    override fun create() {
        batch = SpriteBatch()
        img = Texture("badlogic.jpg")
        camera = OrthographicCamera()
        camera.setToOrtho(false, 480f, 720f)


        card = Card(0f, 0f, 100f, 100f, Color.RED)

        val initState = GameState(0f, 0f, 0f, 1, false)
        store = Gamestore(initState)
        unsubscribe = store.subscribe({ state, dispatch ->
            this.dispatch = dispatch

            Gdx.app.log("Subscribe", "State updated " + state.toString());
            red = state.red
            blue = state.blue
            green = state.green
        })

        card.setX(200f)

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
        super.render()
        Gdx.gl.glClearColor(red, blue, green, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        batch.draw(img, 0f, 0f)


        card.draw(batch, 1f)
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

    companion object {
        @kotlin.jvm.JvmField
        val VERSION = 0.1f
        @kotlin.jvm.JvmField
        val V_WIDTH  = 480
        @kotlin.jvm.JvmField
        val V_HEIGHT = 420
        @kotlin.jvm.JvmField
        val TITLE = "My Game"
    }
}
