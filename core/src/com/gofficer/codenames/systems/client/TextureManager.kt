package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.badlogic.gdx.assets.AssetManager
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.utils.get


class TextureManager(assetManager: AssetManager) : BaseSystem() {

    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    val cardTexture = gameplayAtlas!![RegionNames.CARD]

    override fun processSystem() {
    }

}