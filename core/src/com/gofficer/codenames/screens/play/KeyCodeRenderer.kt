package com.gofficer.codenames.screens.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.utils.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.gofficer.codenames.assets.AssetPaths
import redux.api.Store
import com.gofficer.codenames.redux.actions.ChangeScene
import com.gofficer.codenames.redux.models.Card
import gofficer.codenames.redux.game.GameState


class KeyCodeRenderer(private val myFont: BitmapFont, private val assetManager: AssetManager,
                      private val store: Store<GameState>) : Disposable {

    companion object {
        @JvmStatic
        private val log = logger<KeyCodeRenderer>()
    }

    // == properties ==
    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    private val uiCamera = OrthographicCamera()
    private val uiViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)
    private val renderer = ShapeRenderer()
    private val batch = SpriteBatch()
    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]

    private val uiSkinAtlas = assetManager[AssetDescriptors.UI_SKIN]

    private var skin: Skin = Skin()

    private val stage: Stage = Stage(viewport)

    private var totalCards = 0

    private var cards: List<Card>? = null

    private var subscription: Store.Subscription? = null

    // == public functions ==

    fun show() {
        stage.clear()
        initSkin()
        subscription?.unsubscribe()
        subscription = store.subscribe {
            if (store.state.cards != cards) {
                log.debug("Cards no longer equal, call showLabels")
                stage.clear() // If I don't first clear stage I don't see any change
                showLabels()
            }
        }
        showLabels()

        Gdx.input.inputProcessor = stage
    }

    private fun showLabels() {
        val tableHeader = Table()
        val table = Table()

        val currentState = store.state
        totalCards = currentState.cards.size
        cards = currentState.cards

        tableHeader.add(makeButton("Back to game") {
            log.debug("Pressed quit game")
            // TODO dispatch instead
            store.dispatch(ChangeScene("PlayScreen"))
        })

        if (totalCards >= 25) {
            val blueCards = currentState.cards.filter { it.type == "BLUE" }
            val redCards = currentState.cards.filter { it.type == "RED" }
            val bystanderCards = currentState.cards.filter { it.type == "BYSTANDER" }
            val doubleAgentCards = currentState.cards.filter { it.type == "DOUBLE_AGENT" }

            blueCards.forEach { card ->
                table.row()
                table.add(getLabel(card))
            }
            redCards.forEach { card ->
                table.row()
                table.add(getLabel(card))
            }
            bystanderCards.forEach { card ->
                table.row()
                table.add(getLabel(card))
            }
            doubleAgentCards.forEach { card ->
                table.row()
                table.add(getLabel(card))
            }
        }

        table.setFillParent(true)
        tableHeader.x = 100f
        tableHeader.y = GameConfig.WORLD_HEIGHT - 50
        stage.addActor(tableHeader)
        stage.addActor(table)
    }

    fun render(delta: Float) {
        clearScreen()
        renderGamePlay(delta)
    }

    private fun renderGamePlay(delta: Float) {
        viewport.apply()
        batch.projectionMatrix = camera.combined

        stage.act(delta)
        stage.draw()
    }

    private fun getLabel(card: Card): Label {
        val nameLabel = Label(card.text, skin)
//                nameLabel.style =
        println("Color: ${card.type}")
        nameLabel.color = when (card.type) {
            "RED" -> Color.RED
            "BLUE" -> Color.BLUE
            "BYSTANDER" -> Color.YELLOW
            "DOUBLE_AGENT" -> Color.BLACK
            else -> Color.WHITE
        }

        val currentColor = nameLabel.color

        if (card.isRevealed) {
            currentColor.a = 0.2f
            nameLabel.color = currentColor
        }
        return nameLabel
    }

    fun resize(width: Int, height: Int) {
        log.debug("resize")
        viewport.update(width, height, true)
        uiViewport.update(width, height, true)
    }

    override fun dispose() {
        log.debug("dispose")
        stage.actors.forEach {
            it.remove()
        }
        subscription?.unsubscribe()
        renderer.dispose()
        batch.dispose()
    }

    private fun initSkin() {
        skin.addRegions(uiSkinAtlas)
        skin.add("default-font", myFont)
        skin.load(AssetPaths.UI_SKIN_JSON.toInternalFile())
    }

    private fun makeButton(name: String, onClick: () -> Unit): TextButton {
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