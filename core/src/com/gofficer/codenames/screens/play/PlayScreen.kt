package com.gofficer.codenames.screens.play

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.*
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.components.*
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

    private val camera = OrthographicCamera()
    private val assetManager = game.assetManager
    private var renderer: PlayRenderer = PlayRenderer(game.font24, assetManager, game.store)

    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    private val cardTexture = gameplayAtlas[RegionNames.CARD]

    private val batch = SpriteBatch()
    private val font = BitmapFont()

    private var renderingSystem: RenderingSystem? = null
    private var touchSystem: TouchSystem? = null
    private var animationSystem: FlipAnimationSystem? = null

    override fun show() {

//        batch.projectionMatrix = camera.combined

        renderingSystem = RenderingSystem(batch, font)
        touchSystem = TouchSystem(camera)
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
        val scaleFactor = 0.8f
        game.engine.addEntity(Entity().apply {
            add(TextureComponent(cardTexture))
            add(TransformComponent(Vector2(0f, 0f)))
            add(RevealableComponent())
            add(StateComponent())
            add(TeamComponent(Color.RED))
            add(NameComponent("Some name"))
            add(RectangleComponent(cardTexture!!.regionWidth.toFloat() * scaleFactor, cardTexture!!.regionHeight.toFloat() * scaleFactor))
            add(ClickableComponent())
        })

        game.engine.addEntity(Entity().apply {
            add(TextureComponent(cardTexture))
            add(TransformComponent(Vector2(300f, 0f)))
            add(RevealableComponent())
            add(StateComponent())
            add(TeamComponent(Color.RED))
            add(NameComponent("Some other name"))
            add(RectangleComponent(cardTexture!!.regionWidth.toFloat() * scaleFactor, cardTexture!!.regionHeight.toFloat() * scaleFactor))
            add(ClickableComponent())
        })
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
}
