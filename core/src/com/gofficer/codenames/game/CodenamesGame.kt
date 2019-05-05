package com.gofficer.codenames.game

import com.badlogic.gdx.*
import com.badlogic.gdx.assets.AssetManager
import com.gofficer.codenames.redux.Dispatch
import com.gofficer.codenames.redux.Unsubscribe
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Logger
import com.gofficer.codenames.screens.game.PlayScreen
import com.gofficer.codenames.screens.loading.LoadingScreen
import com.gofficer.codenames.utils.logger
import com.gofficer.sampler.utils.toInternalFile

class CodenamesGame : Game() {

    companion object {
        @JvmStatic
        private val log = logger<CodenamesGame>()
    }

    val assetManager = AssetManager()

    //
//    internal lateinit var batch: SpriteBatch
//    internal lateinit var img: Texture
//    internal var red: Float = 0f
//    internal var green: Float = 0f
//    internal var blue: Float = 0f
//    lateinit var card: Card
//    lateinit var camera: OrthographicCamera
//
    lateinit var font24: BitmapFont
//
//    var splashScreen: SplashScreen? = null
//    var loadingScreen: LoadingScreen? = null
//    var mainMenuScreen: MainMenuScreen? = null
//    var playScreen: PlayScreenOld? = null
//
//    lateinit var assets: AssetManager


    internal lateinit var store: Gamestore
    private var unsubscribe: Unsubscribe? = null
    private var dispatch: Dispatch? = null
    override fun create() {
        Gdx.app.logLevel = Application.LOG_DEBUG
        assetManager.logger.level = Logger.DEBUG
        log.debug("create")

        initFonts()

        setScreen(LoadingScreen(this))
//
//        log.debug("create()")
//
//        assets = AssetManager()
//        camera = OrthographicCamera()
//        camera.setToOrtho(false, 480f, 720f)
//        batch = SpriteBatch()
//
//        initFonts()
//
//        loadingScreen = LoadingScreen(this)
//        splashScreen = SplashScreen(this)
//        mainMenuScreen = MainMenuScreen(this)
//        playScreen = PlayScreenOld(this)


//        this.setScreen(loadingScreen);
//          card = Card(0f, 0f, 100f, 100f, Color.RED)
//
//        val initState = GameState(0f, 0f, 0f, 1, false)
//        store = Gamestore(initState)
//        unsubscribe = store.subscribe({ state, dispatch ->
//            this.dispatch = dispatch
//
//            Gdx.app.log("Subscribe", "State updated " + state.toString());
//            red = state.red
//            blue = state.blue
//            green = state.green
//        })
//
//        card.setX(200f)
//
//        Gdx.input.inputProcessor = object : InputAdapter() {
//            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
//                if (button == Input.Buttons.LEFT) {
//                    // do something
//                    Gdx.app.log("ApplicationAdapter", "Some Log")
//                    var rand1 = Math.round( Math.random() ).toFloat()
//                    var rand2 = Math.round( Math.random() ).toFloat()
//                    var rand3 = Math.round( Math.random() ).toFloat()
//                    store.dispatch(ChangeColor(rand1, rand2, rand3))
//                }
//                return false
//            }
//        }

    }

//    override fun render() {
//        super.render()
//        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
//            dispose()
//            create()
//        }
//    }

//
//    override fun render() {
//        super.render()
//
//        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
//            Gdx.app.exit();
//        }
////        Gdx.gl.glClearColor(red, blue, green, 1f)
////        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
////        batch.begin()
////        batch.draw(img, 0f, 0f)
////
////
////        card.draw(batch, 1f)
////        batch.end()
////
////
////        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
////            dispose()
////            create()
////        }
//    }

    override fun dispose() {
        super.dispose()

        log.debug("dispoase")
        assetManager.dispose()
//        batch.dispose()
        font24.dispose()
//        assets.dispose()
//        loadingScreen?.dispose()
//        splashScreen?.dispose()
//        mainMenuScreen?.dispose()
//        playScreen?.dispose()
    }

    private fun initFonts() {
        val generator = FreeTypeFontGenerator("fonts/Arcon.ttf".toInternalFile())
        val params = FreeTypeFontGenerator.FreeTypeFontParameter()

        params.size = 24
        params.color = Color.BLACK
        font24 = generator.generateFont(params)
    }

}
