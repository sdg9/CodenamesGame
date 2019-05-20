package com.gofficer.codenames.config

object GameConfig {

    const val WIDTH = 1280 // pixels - desktop only
    const val HEIGHT = 720 // pixels - desktop only

    const val WORLD_WIDTH = 1280f // world units
    const val WORLD_HEIGHT = 720f // world units

    const val HUD_WIDTH = 1280 / 2f // world units, 1 : 1 ppu
    const val HUD_HEIGHT = 720 / 2f // world units, 1 : 1 ppu

    const val WORLD_CENTER_X = WORLD_WIDTH / 2f
    const val WORLD_CENTER_Y = WORLD_HEIGHT / 2f

    const val OBSTACLE_SPAWN_TIME = 0.35f
    const val LIVES_START = 3

    const val SCORE_MAX_TIME = 1.25f

    const val EASY_OBSTACLE_SPEED = 0.1f
    const val MEDIUM_OBSTACLE_SPEED = 0.15f
    const val HARD_OBSTACLE_SPEED = 0.18f

    const val USE_SPLASH = false

    const val VERSION = 0.1f
    const val TITLE = "Codenames Game"

    const val DEFAULT_FPS_LIMIT = 60

    const val LOCAL_WEBSOCKET_ANDROID = "ws://10.0.2.2:2567"
    const val LOCAL_WEBSOCKET_DESKTOP = "ws://localhost:2567"
//const val LOCAL_WEBSOCKET_DESKTOP = "ws://localhost:55655"
//    const val LOCAL_WEBSOCKET_DESKTOP = "ws://localhost:8899"

//    const val LOCAL_WEBSOCKET_DESKTOP = "ws://codenames-ktor.herokuapp.com/"


    // In whistle
    // from command line run w2
    // make sure rule "localhost:8899 localhost:2567" exists
    // don't use it for anything else and we should be fine as reverse proxy

}