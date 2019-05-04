package com.gofficer.codenames.assets

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.gofficer.codenames.utils.assetDescriptor

/**
 * @author goran on 5/11/2017.
 */
object AssetDescriptors {

    val FONT = assetDescriptor<BitmapFont>(AssetPaths.ALGERIAN_FONT)
    val GAMEPLAY = assetDescriptor<TextureAtlas>(AssetPaths.GAMEPLAY)
    val SPLASH = assetDescriptor<TextureAtlas>(AssetPaths.SPLASH)
    val UI_SKIN = assetDescriptor<TextureAtlas>(AssetPaths.UI_SKIN)
}