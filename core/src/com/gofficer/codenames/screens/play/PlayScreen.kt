package com.gofficer.codenames.screens.play

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.*
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.AssetPaths
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.components.*
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.redux.actions.ChangeScene
import com.gofficer.codenames.systems.FlipAnimationSystem
import com.gofficer.codenames.systems.RenderingSystem
import com.gofficer.codenames.systems.TouchSystem
import com.gofficer.codenames.utils.*
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.log.debug
import ktx.scene2d.*
import java.util.ArrayList

class PlayScreen(val game: CodenamesGame) : KtxScreen {

    companion object {
        @JvmStatic
        private val log = logger<PlayScreen>()
    }

    private val assetManager = game.assetManager

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
    private val uiCamera = OrthographicCamera()
    private val uiViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)
    val batch = SpriteBatch()

    private val uiSkinAtlas = assetManager[AssetDescriptors.UI_SKIN]

    private var skin: Skin = Skin()
    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    private val cardTexture = gameplayAtlas[RegionNames.CARD]

    private val stage: Stage = Stage(viewport)
    private val font = BitmapFont()

    private var renderingSystem: RenderingSystem? = null
    private var touchSystem: TouchSystem? = null
    private var animationSystem: FlipAnimationSystem? = null

    override fun show() {
        renderingSystem = RenderingSystem(batch, font)
        touchSystem = TouchSystem(camera)
        animationSystem = FlipAnimationSystem()
        game.engine.addSystem(renderingSystem)
        game.engine.addSystem(touchSystem)
        game.engine.addSystem(animationSystem)

        debug { "show" }
        createEntities()
//        if (game.client == null) {
//            setupGame()
//        }
//        game.store.subscribe {
//            log.debug("Update to store")
//        }
        stage.clear()
        initSkin()
        initButtons()
    }
    private fun initSkin() {
        skin.addRegions(uiSkinAtlas)
        skin.add("default-font", game.font24)
        skin.load(AssetPaths.UI_SKIN_JSON.toInternalFile())

        Scene2DSkin.defaultSkin = skin
    }

    private fun initButtons() {
        var root = container {
            setFillParent(true) // apparently same as setting size to width/height
            align(Align.top)
            padTop(30f)
            horizontalGroup {
                space(8f)
                textButton("Rematch") {
                    // TODO why do none of these sizes work?
//                    setSize(100f, 100f)
                    height = 100f
                    width = 100f
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            game.store.dispatch(ChangeScene("PlayOnline"))
                        }
                    })
                }
                space(8f)
                textButton("View Key") {
//                    setSize(100f, 100f)
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            game.store.dispatch(ChangeScene("Play"))
                        }
                    })
                }
                space(8f)
                textButton("Quit Game") {
//                    setSize(buttonWidth, buttonHeight)
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            Gdx.app.exit()
                        }
                    })
                }
            }

        }

        stage.addActor(root)
//        stage.isDebugAll = true
        Gdx.input.inputProcessor = stage
    }

    private fun createEntities() {
        val cards = getXUniqueCards(25)
        for (i in 0..4) {
            for (j in 0..4) {
                val card = cards.get(i + j * 5)
              game.engine.addEntity(createCardAtCoordinate(card.text, card.type, i, j))
            }
        }
    }

    override fun render(delta: Float) {
        clearScreen()
        viewport.apply()
        batch.projectionMatrix = camera.combined

//        stage.act(delta)
        stage.draw()

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            hide()
            show()
        }
    }

    override fun resize(width: Int, height: Int) {
        log.debug("resize")
        viewport.update(width, height, true)
        uiViewport.update(width, height, true)
    }

    override fun hide() {
        log.debug("hide")
        game.engine.removeAllEntities()
        dispose()
    }

    override fun dispose() {
        log.debug("dispose")

        game.engine.removeSystem(renderingSystem)
        game.engine.removeSystem(touchSystem)
        game.engine.removeSystem(animationSystem)

        batch.disposeSafely()
    }

    private val scaleFactor = 0.7f
    private fun createCardAtCoordinate(name: String, color: Color, row: Int, column: Int): Entity {

        val width = cardTexture!!.regionWidth.toFloat() * scaleFactor
        val height = cardTexture!!.regionHeight.toFloat() * scaleFactor

        val x = 0f + row * GameConfig.WORLD_WIDTH / 6 + width / 2
        val y = GameConfig.WORLD_HEIGHT - height - ((column + 1) * GameConfig.WORLD_HEIGHT / 6)

        return createCard(name, color, x, y)
    }

    private fun createCard(name: String, color: Color, x: Float, y: Float): Entity {
        return game.engine.createEntity().apply {
            add(TextureComponent(cardTexture))
            add(TransformComponent(Vector2(x, y)))
            add(RevealableComponent())
            add(StateComponent())
            add(TeamComponent(color))
            add(NameComponent(name))
            add(
                RectangleComponent(
                    cardTexture!!.regionWidth.toFloat() * scaleFactor,
                    cardTexture!!.regionHeight.toFloat() * scaleFactor
                )
            )
            add(
                ClickableComponent(
                    cardTexture!!.regionWidth.toFloat() * scaleFactor,
                    cardTexture!!.regionHeight.toFloat() * scaleFactor
                )
            )
        }
    }

    private fun getRandomArbitrary(min: Int, max: Int): Int {
        return Math.floor(Math.random() * (max - min)).toInt() + min
    }

    private fun getXUniqueCards(count: Int): List<ECSCard> {
        println("Getting $count unique cards")
        val cards = mutableListOf<ECSCard>()
        var attempts = 0

        val isBlueFirst = Math.random() > 0.5
        val totalBlue = if (isBlueFirst) 9 else 8
        val totalRed = if (!isBlueFirst) 9 else 8
//        val types = mutableListOf<String>()
        val types = mutableListOf<Color>()

        for (i in 1..totalBlue) {
            types.add(Color.BLUE)
        }
        for (i in 1..totalRed) {
            types.add(Color.RED)
        }
        types.add(Color.BLACK);
        while (types.size < 25) {
            types.add(Color.BROWN)
        }
        val shuffledTypes = types.shuffled()

        while (cards.size < count) {
//        println("Adding more: ${cards.size}")
            val random = getRandomArbitrary(0, vanillaWordList.size)
//        println("Random: $random")
            val exists = cards.any { it.text == vanillaWordList[random] }
            if (!exists) {
                cards.add(ECSCard(cards.size + 1, vanillaWordList.get(random), shuffledTypes.get(cards.size), false))
            }
            attempts += 1
            if (attempts > 200) {
                break
            }
        }
        return cards
    };
}

data class ECSCard(
    val id: Int,
    val text: String,
    val type: Color,
    val isRevealed: Boolean = false)

fun List<ECSCard>.update(card: ECSCard): List<ECSCard> {
    var index = -1
    forEachIndexed { i, s ->
        if (s.id == card.id) {
            index = i
            return@forEachIndexed
        }
    }

    if (index != -1) {
        val mutable = ArrayList(this)
        mutable.removeAt(index)
        mutable.add(index, card)
        return mutable
    }

    return this
}

fun List<ECSCard>.getById(id: Int): ECSCard? {
    forEach {
        if (id == it.id) {
            return it
        }
    }

    return null
}
