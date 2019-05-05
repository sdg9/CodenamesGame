package com.gofficer.codenames.screens.menu


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.FitViewport

import com.badlogic.gdx.scenes.scene2d.ui.*
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.AssetPaths
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.game.CodenamesGame
import com.gofficer.codenames.screens.game.PlayScreen
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.logger
import com.gofficer.codenames.utils.toInternalFile



class MainMenuScreen(private val game: CodenamesGame) : ScreenAdapter() {

    companion object {
        @JvmStatic
        private val log = logger<MainMenuScreen>()
    }

    private var skin: Skin = Skin()

    private lateinit var buttonPlay: TextButton
    private lateinit var buttonExit: TextButton

    private val renderer: ShapeRenderer = ShapeRenderer()

    private val camera = OrthographicCamera()
    private val uiCamera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    private val uiViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)

    private val stage: Stage = Stage(uiViewport)

    private val uiSkinAtlas = game.assetManager[AssetDescriptors.UI_SKIN]


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
        skin.load(AssetPaths.UI_SKIN_JSON.toInternalFile())
    }

    private fun initButtons() {

        val mainTable = Table()

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

        val buttonWidth = GameConfig.HUD_WIDTH * .6f
        val buttonHeight = GameConfig.HUD_HEIGHT / 10
//
//        stage.addActor(buttonPlay)
//        stage.addActor(buttonExit)
        val nameLabel = Label("Name:", skin)
        val nameText = TextField("", skin)
        val addressLabel = Label("Address:", skin)
        val addressText = TextField("", skin)

        val table = Table()
        table.add(buttonPlay).width(buttonWidth).height(buttonHeight)
        table.row()
        table.add().height(GameConfig.HUD_HEIGHT / 5)
        table.row()
        table.add(buttonExit).width(buttonWidth).height(buttonHeight)
        table.setFillParent(true)
//        mainTable.add(buttonPlay)
//        mainTable.add(buttonExit)
        stage.addActor(table)
    }


    private fun makeButton(name: String, positionY: Float, onClick: () -> Unit): TextButton {
        return TextButton(name, skin, "default").apply {
            // TODO: figure out how to better deal with font, as-is this distorts bitmap
//            label.setFontScale(1f)
//            setSize(GameConfig.WORLD_WIDTH / 2, GameConfig.WORLD_HEIGHT / 5)
//            setPosition(GameConfig.WORLD_CENTER_X, positionY, Align.center)
//            addAction(sequence(alpha(0f), parallel(fadeIn(.5f), moveBy(0f, -20f, .5f, Interpolation.pow5Out))))
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    onClick()
                }
            })
        }
    }
}