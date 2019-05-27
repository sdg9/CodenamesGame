package com.gofficer.codenames

import com.badlogic.ashley.core.*
import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Logger
import com.daveanthonythomas.moshipack.MoshiPack
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Server
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.systems.RemoveSystem
import com.gofficer.codenames.utils.logger
import com.gofficer.colyseus.client.Room
import com.gofficer.sampler.utils.toInternalFile
import gofficer.codenames.redux.game.GameState
import ktx.app.KtxGame
import ktx.app.KtxScreen

class CodenamesGame : KtxGame<KtxScreen>() {

    companion object {
        @JvmStatic
        private val log = logger<CodenamesGame>()
    }

    var room: Room? = null
//    var client: Client? = null
    val assetManager = AssetManager()

    lateinit var font24: BitmapFont
    private val initState: GameState = GameState()

    private val LATENCY_MIN = 100f // ms
    private val LATENCY_MAX = 500f // ms

    private val LERP_MIN = 0.1f
    private val LERP_MAX = 0.5f
    private var lerp = LERP_MAX
    private lateinit var game: CodenamesGame

    var client: Client? = null
    var server: Server? = null

    val engine = Engine()

//    val world = World(engine, assetManager)

    private val moshiPack = MoshiPack()

    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        assetManager.logger.level = Logger.DEBUG
        log.debug("create")

        initFonts()

        game = this

        addScreen(LoadingScreen(game))
        setScreen<LoadingScreen>()

        engine.addSystem(RemoveSystem())
//        engine.addSystem(AnimatingSystem())
//        engine.addSystem(SomeSystem())

//        createEntities()
    }


    override fun render() {
        super.render()
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        super.dispose()

        log.debug("dispoase")
        assetManager.dispose()
        font24.dispose()
    }

    private fun initFonts() {
        val generator = FreeTypeFontGenerator("fonts/Arcon.ttf".toInternalFile())
        val params = FreeTypeFontGenerator.FreeTypeFontParameter()

        params.size = 24
        params.color = Color.BLACK
        font24 = generator.generateFont(params)
    }
}
