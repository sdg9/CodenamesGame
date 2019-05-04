package com.gofficer.codenames.desktop

import com.badlogic.gdx.tools.texturepacker.TexturePacker

/**
 * @author goran on 5/11/2017.
 */
object AssetPacker {

    const val DRAW_DEBUG_OUTLINE = false
    const val RAW_ASSETS_PATH = "core/assets-raw"
    const val ASSETS_PATH = "android/assets"
}

fun main(args: Array<String>) {
    packFolder("gameplay")
    packFolder("splash")
}

fun packFolder(folder: String) {
    val settings = TexturePacker.Settings().apply {
        debug = AssetPacker.DRAW_DEBUG_OUTLINE
    }
    TexturePacker.process(settings,
            "${AssetPacker.RAW_ASSETS_PATH}/$folder",
            "${AssetPacker.ASSETS_PATH}/$folder",
            folder)
}