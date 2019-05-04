package com.gofficer.codenames.config

object GameConfig {

    const val WIDTH = 480 // pixels - desktop only
    const val HEIGHT = 800 // pixels - desktop only

    const val WORLD_WIDTH = 600.0f // world units
    const val WORLD_HEIGHT = 1000.0f // world units

    const val HUD_WIDTH = 480f // world units, 1 : 1 ppu
    const val HUD_HEIGHT = 800f // world units, 1 : 1 ppu

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
}