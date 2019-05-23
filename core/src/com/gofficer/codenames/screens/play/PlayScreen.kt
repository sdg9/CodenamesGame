package com.gofficer.codenames.screens.play

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.*
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.redux.actions.SetupGame
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.utils.logger
import ktx.app.KtxScreen
import ktx.ashley.add
import ktx.ashley.entity
import ktx.ashley.mapperFor

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

    private var renderingSystem: RenderingSystem? = null
    private var touchSystem: TouchSystem? = null

    override fun show() {

//        batch.projectionMatrix = camera.combined

        renderingSystem = RenderingSystem(batch)
        touchSystem = TouchSystem(camera)
        game.engine.addSystem(renderingSystem)
        game.engine.addSystem(touchSystem)

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

//        game.engine.add {
//            entity {
//                with<Transform> {
//                    position = Vector2(0f, 0f)
//                }
//                with<TextureComponent> {
//                    texture = cardTexture
//                }
//            }
//        }

        game.engine.addEntity(Entity().apply {
            add(TextureComponent(cardTexture))
            add(Transform(Vector2(0f, 0f)))
            add(Revealable())
            add(Clickable())
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
        renderer.dispose()
    }

    private fun setupGame() {
        game.store.dispatch(SetupGame())
    }
}
