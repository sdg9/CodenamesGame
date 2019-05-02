package com.gofficer.codenames.screens

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Align
import com.gofficer.codenames.actions.FlipAction
import com.gofficer.codenames.actors.Card
import com.gofficer.codenames.game.Application
import com.gofficer.codenames.actors.SlideButton
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.logger


class PlayScreen(// App reference
        private val app: Application) : Screen {

    companion object {
        @JvmStatic
        private val log = logger<PlayScreen>()
    }

    // Stage vars
    private val stage: Stage
    private var skin: Skin? = null

    // Game Grid
    private val boardSize = 4
    private var holeX: Int = 0
    private var holeY: Int = 0
//    private lateinit var buttonGrid: Array<Array<SlideButton>>
//    private lateinit var buttonGrid: Array<Array<SlideButton>>

    // Nav-Buttons
    private var buttonBack: TextButton? = null

    // Info label
    private var labelInfo: Label? = null

    init {
        this.stage = Stage(FitViewport(Application.V_WIDTH, Application.V_HEIGHT, app.camera))
    }

    override fun show() {
        println("PLAY")
        Gdx.input.inputProcessor = stage
        stage.clear()

        this.skin = Skin()
        this.skin?.addRegions(app.assets.get("ui/uiskin.atlas", TextureAtlas::class.java))
        this.skin?.add("default-font", app.font24)
        this.skin?.load(Gdx.files.internal("ui/uiskin.json"))

        initNavigationButtons()
        initInfoLabel()
        initGrid()
//        shuffle()
    }

//    private fun shuffle() {
//        var swaps = 0 // debug variable
//        var shuffles: Int
//        // 99 is arbitrary
//        shuffles = 0
//        while (shuffles < 99) {
//            // Choose a random spot in the grid and check if a valid move can be made
//            val posX = MathUtils.random(0, boardSize - 1)
//            val posY = MathUtils.random(0, boardSize - 1)
//            if (holeX == posX || holeY == posX) {
//                moveButtons(posX, posY)
//                swaps++
//            }
//            shuffles++
//        }
//        println("Tried: $shuffles, actual moves made: $swaps") // Debug logging
//    }

    private fun update(delta: Float) {
        stage.act(delta)
    }

    override fun render(delta: Float) {
        clearScreen()

        update(delta)

        stage.draw()

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            hide()
            show()
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun hide() {

    }

    override fun dispose() {
        stage.dispose()
    }

    // Initialize the back button
    private fun initNavigationButtons() {
        buttonBack = TextButton("Back", skin!!, "default")
        buttonBack?.setPosition(20f, app.camera.viewportHeight - 70)
        buttonBack?.setSize(100f, 50f)
        buttonBack?.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                app.setScreen(app.mainMenuScreen)
            }
        })

        stage.addActor(buttonBack)
    }

    // Initialize the info label
    private fun initInfoLabel() {
        labelInfo = Label("Welcome! Click any number tile to begin!", skin!!, "default")
        labelInfo?.setPosition(25f, 310f)
        labelInfo?.setAlignment(Align.center)
        labelInfo?.addAction(sequence(alpha(0f), delay(.5f), fadeIn(.5f)))
        stage.addActor(labelInfo)
    }

    // Initialize the game grid
    private fun initGrid() {
        val id = 4

        val button = skin?.let {
            SlideButton(id.toString() + "", it, "default", id)
        }

        val card = Card(0f, 0f, 20f, 20f, Color.RED)
//        button?.setOrigin(button.width / 2, button.height / 2)
        stage.addActor(button)
        stage.addActor(card)

        card.addAction(
                SequenceAction(
                        FlipAction.flipOut(0f, 10f, 1f),
                        FlipAction.flipIn(0f, 10f, 1f)
                )
        )


//        val id2 = 5
//        stage.addActor(skin?.let { SlideButton(id2.toString() + "", it, "default", id2) })
//        stage.actors[1].x = 100f
//        stage.actors[1].moveBy(50f, 10f)
        button?.addAction(moveTo(100f, 0f, 1f))
//        button?.addAction(rotateBy(320f, 1f))

//        stage.actors[1].x = 40f

//        val nums = Array<Int>()

//        val nums = Array<Int>()
//        buttonGrid = arrayOf<Array<SlideButton>>()
//        buttonGrid = Array<Array<SlideButton>>(boardSize) { Array<SlideButton>(boardSize)}
//        buttonGrid = Array<Array<SlideButton>>(boardSize) { arrayOfNulls<SlideButton>(boardSize) }
//        buttonGrid = Array<Array<SlideButton>>()

//        val nums = Array<Int>()
//        buttonGrid = Array<Array<SlideButton>>(boardSize) { arrayOfNulls<SlideButton>(boardSize) }
//
//        // Initialize the grid array
//        for (i in 1 until boardSize * boardSize) {
//            nums.add(i)
//        }
//
//
////        buttonGrid = Array<Slide>
////        buttonGrid = SlideButtoneButton2([boardSize][boardSize]);
////        buttonGrid = Array<Array<SlideButton>>(boardSize) { arrayOfNulls<SlideButton>(boardSize) }
//
//        // Initialize the grid array
//        for (i in 1 until boardSize * boardSize) {
//            nums.add(i)
//        }
//
//        // Set the hole at the bottom right so the sequence is 1,2,3...,15,hole (solved state) from which to start shuffling.
//        holeX = boardSize - 1
//        holeY = boardSize - 1
//
//        for (i in 0 until boardSize) {
//            for (j in 0 until boardSize) {
//                if (i != holeY || j != holeX) {
//                    val id = nums.removeIndex(0)
//                    buttonGrid!![i][j] = skin?.let { SlideButton(id.toString() + "", it, "default", id) }
//                    buttonGrid!![i][j].setPosition(app.camera.viewportWidth / 7 * 2 + 51 * j,
//                            app.camera.viewportHeight / 5 * 3 - 51 * i)
//                    buttonGrid!![i][j].setSize(50f, 50f)
//                    buttonGrid!![i][j].addAction(sequence(alpha(0f), delay((j + 1 + i * boardSize) / 60f),
//                            parallel(fadeIn(.5f), moveBy(0f, -10f, .25f, Interpolation.pow5Out))))
//
//                    // Slide/Move Button
//                    buttonGrid!![i][j].addListener(object : ClickListener() {
//                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
//                            var buttonX = 0
//                            var buttonY = 0
//                            var buttonFound = false
//                            val selectedButton = event!!.listenerActor as SlideButton
//
//                            run {
//                                var i = 0
//                                while (i < boardSize && !buttonFound) {
//                                    run {
//                                        var j = 0
//                                        while (j < boardSize && !buttonFound) {
//                                            if (buttonGrid!![i][j] != null && selectedButton === buttonGrid!![i][j]) {
//                                                buttonX = j
//                                                buttonY = i
//                                                buttonFound = true
//                                            }
//                                            j++
//                                        }
//                                    }
//                                    i++
//                                }
//                            }
//
//                            if (holeX == buttonX || holeY == buttonY) {
//                                moveButtons(buttonX, buttonY)
//
//                                if (solutionFound()) {
//                                    labelInfo!!.clearActions()
//                                    labelInfo!!.setText("Solution Found!")
//                                    labelInfo!!.addAction(sequence(alpha(1f), delay(3f), fadeOut(2f, Interpolation.pow5Out)))
//                                }
//                            } else {
//                                labelInfo!!.clearActions()
//                                labelInfo!!.setText("Invalid Move!")
//                                labelInfo!!.addAction(sequence(alpha(1f), delay(1f), fadeOut(1f, Interpolation.pow5Out)))
//                            }
//                        }
//                    })
//                    stage.addActor(buttonGrid!![i][j])
//                }
//            }
//        }
    }

//    private fun moveButtons(x: Int, y: Int) {
//        var button: SlideButton
//        if (x < holeX) {
//            while (holeX > x) {
//                button = buttonGrid!![holeY][holeX - 1]
//                button.addAction(moveBy(51f, 0f, .5f, Interpolation.pow5Out))
//                buttonGrid!![holeY][holeX] = button
//                buttonGrid!![holeY][holeX - 1] = null
//                holeX--
//            }
//        } else {
//            while (holeX < x) {
//                button = buttonGrid!![holeY][holeX + 1]
//                button.addAction(moveBy(-51f, 0f, .5f, Interpolation.pow5Out))
//                buttonGrid!![holeY][holeX] = button
//                buttonGrid!![holeY][holeX + 1] = null
//                holeX++
//            }
//        }
//
//        if (y < holeY) {
//            while (holeY > y) {
//                button = buttonGrid!![holeY - 1][holeX]
//                button.addAction(moveBy(0f, -51f, .5f, Interpolation.pow5Out))
//                buttonGrid!![holeY][holeX] = button
//                buttonGrid!![holeY - 1][holeX] = null
//                holeY--
//            }
//        } else {
//            while (holeY < y) {
//                button = buttonGrid!![holeY + 1][holeX]
//                button.addAction(moveBy(0f, 51f, .5f, Interpolation.pow5Out))
//                buttonGrid!![holeY][holeX] = button
//                buttonGrid!![holeY + 1][holeX] = null
//                holeY++
//            }
//        }
//    }
//
//    private fun solutionFound(): Boolean {
//        var idCheck = 1
//        for (i in 0 until boardSize) {
//            for (j in 0 until boardSize) {
//                if (buttonGrid!![i][j] != null) {
//                    if (buttonGrid!![i][j].id === idCheck++) {
//                        if (idCheck == 16) {
//                            return true
//                        }
//                    } else {
//                        return false
//                    }
//                } else {
//                    return false
//                }
//            }
//        }
//        return false
//    }
}
