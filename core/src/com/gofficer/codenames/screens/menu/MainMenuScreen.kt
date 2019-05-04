package com.gofficer.codenames.screens.menu


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport

import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.utils.Align
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.game.CodenamesGame
import com.gofficer.codenames.screens.game.PlayScreen
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.utils.logger
import com.gofficer.codenames.utils.toInternalFile

class MainMenuScreen(private val game: CodenamesGame) : ScreenAdapter() {

    companion object {
        @JvmStatic
        private val log = logger<MainMenuScreen>()
    }

    private var skin: Skin = Skin()

    lateinit private var buttonPlay: TextButton
    lateinit private var buttonExit: TextButton

    private val renderer: ShapeRenderer = ShapeRenderer()

    private val camera = OrthographicCamera()
    private val stage: Stage = Stage(FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera))
    private val gameplayAtlas = game.assetManager[AssetDescriptors.GAMEPLAY]
    private val uiSkinAtlas = game.assetManager[AssetDescriptors.UI_SKIN]
    private val backgroundTexture = gameplayAtlas[RegionNames.BACKGROUND]

    private val font = game.assetManager[AssetDescriptors.FONT]

//    private var splashImg: Image = Image(splashIconTexture)

    override fun show() {
        log.debug("show")
        Gdx.input.inputProcessor = stage
        stage.clear()

        initSkin()
        initButtons()
    }

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

    override fun dispose() {
        stage.dispose()
        renderer.dispose()
    }

    // === private ===


    private fun initSkin() {
        skin.addRegions(uiSkinAtlas)
        skin.add("default-font", game.font24)
        skin.load("ui/uiskin.json".toInternalFile())
    }

    private fun initButtons() {

        buttonPlay = makeButton(
                "Play",
                GameConfig.WORLD_CENTER_Y + GameConfig.WORLD_HEIGHT / 5
        ) {
            log.debug("Pressed playa")
            game.screen = PlayScreen(game)
        }

        buttonExit = makeButton(
                "Exit",
                GameConfig.WORLD_CENTER_Y - GameConfig.WORLD_HEIGHT / 5
        ) { Gdx.app.exit() }


        stage.addActor(buttonPlay)
        stage.addActor(buttonExit)
    }

    private fun makeButton(name: String, positionY: Float, onClick: () -> Unit): TextButton {
        return TextButton(name, skin, "default").apply {
            setSize(GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 5)
            setPosition(GameConfig.WORLD_CENTER_X, positionY, Align.center)
            addAction(sequence(alpha(0f), parallel(fadeIn(.5f), moveBy(0f, -20f, .5f, Interpolation.pow5Out))))
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    onClick()
                }
            })
        }
    }
}