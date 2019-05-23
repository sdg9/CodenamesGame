package com.gofficer.codenames

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.components.*
import com.gofficer.codenames.utils.get

class World(val engine: Engine, assetManager: AssetManager) {

    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    private val cardTexture = gameplayAtlas[RegionNames.CARD]

//    fun create() {
//        // TODO create world
//        // In case of codenames, 25 cards
//        generateCards()
//    }
//
//    private fun generateCards() {
//        createCard()
//    }
//
//    private fun createCard(): Entity {
//        return engine.createEntity().apply {
//            add(TextureComponent(cardTexture))
//            add(TransformComponent(Vector2(0f, 0f)))
//            add(RevealableComponent())
//            add(StateComponent())
//            add(NameComponent("Test"))
//            add(RectangleComponent(cardTexture!!.regionWidth.toFloat(), cardTexture!!.regionHeight.toFloat()))
//            add(ClickableComponent())
//        }
//    }
}