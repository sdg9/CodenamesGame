package com.gofficer.codenames.screens.play

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.*
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.components.*
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.redux.actions.SetupGame
import com.gofficer.codenames.systems.FlipAnimationSystem
import com.gofficer.codenames.systems.RenderingSystem
import com.gofficer.codenames.systems.TouchSystem
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.utils.logger
import ktx.app.KtxScreen

class PlayScreen(val game: CodenamesGame) : KtxScreen {

    companion object {
        @JvmStatic
        private val log = logger<PlayScreen>()
    }

//    private val camera = OrthographicCamera()
    private val assetManager = game.assetManager
    private var renderer: PlayRenderer = PlayRenderer(game.font24, assetManager, game.store)

    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    private val cardTexture = gameplayAtlas[RegionNames.CARD]

//    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private var renderingSystem: RenderingSystem? = null
    private var touchSystem: TouchSystem? = null
    private var animationSystem: FlipAnimationSystem? = null

    override fun show() {

//        batch.projectionMatrix = camera.combined

        renderingSystem = RenderingSystem(renderer.batch, font)
        touchSystem = TouchSystem(renderer.camera)
        animationSystem = FlipAnimationSystem()
        game.engine.addSystem(renderingSystem)
        game.engine.addSystem(touchSystem)
        game.engine.addSystem(animationSystem)

        log.debug("show")

        createEntities()
        if (game.client == null) {
            setupGame()
        }
        game.store.subscribe {
            log.debug("Update to store")
        }
        renderer.show()
    }

    private fun createEntities() {

        for (i in 0..4) {
            for (j in 0..4) {
//                val entity = createCard("Test $i", Color.BLUE, 0f + (i * GameConfig.WORLD_WIDTH / 5), GameConfig.WORLD_HEIGHT - (j * GameConfig.WORLD_HEIGHT / 5) - 200)
                game.engine.addEntity(createCardAtCoordinate("Test $i:$j", Color.BLUE, i, j))
            }
        }
//        game.engine.addEntity(createCard("Test 2", Color.RED, 200f, 0f))
    }

    override fun render(delta: Float) {
//        controller.update(delta)
        renderer.render(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            hide()
            show()
        }
    }

    override fun resize(width: Int, height: Int) {
        log.debug("resize")
        renderer.resize(width, height)
    }

    override fun hide() {
        log.debug("hide")
        dispose()
    }

    override fun dispose() {
        log.debug("dispose")

        game.engine.removeSystem(renderingSystem)
        game.engine.removeSystem(touchSystem)
        game.engine.removeSystem(animationSystem)
        renderer.dispose()
    }

    private fun setupGame() {
        game.store.dispatch(SetupGame())
    }

    val scaleFactor = 0.7f
    fun createCardAtCoordinate(name: String, color: Color, row: Int, column: Int): Entity {

        val width = cardTexture!!.regionWidth.toFloat() * scaleFactor
        val height = cardTexture!!.regionHeight.toFloat() * scaleFactor

        val x = 0f + row * GameConfig.WORLD_WIDTH / 6 + width / 2
        val y = GameConfig.WORLD_HEIGHT - height - ((column + 1) * GameConfig.WORLD_HEIGHT / 6)

        return createCard(name, color, x, y)
    }

    fun createCard(name: String, color: Color, x: Float, y: Float): Entity {
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
}
