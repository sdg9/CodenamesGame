package com.gofficer.codenames

import com.artemis.ComponentMapper
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.components.*
import com.gofficer.codenames.screens.loading.LoadingScreen
import ktx.log.logger

class EntityFactory(val gameWorld: GameWorld) {

    companion object {
        private val log = logger<LoadingScreen>()
    }

    private lateinit var mTransform: ComponentMapper<TransformComponent>
    private lateinit var mTextureReference: ComponentMapper<TextureReferenceComponent>
    private lateinit var mCard: ComponentMapper<CardComponent>
    private lateinit var mPlayer: ComponentMapper<PlayerComponent>
    private lateinit var mPosition: ComponentMapper<PositionComponent>
    private lateinit var mNetworkComponent: ComponentMapper<NetworkComponent>
    private lateinit var mRectangle: ComponentMapper<RectangleComponent>

    val artemisWorld = gameWorld.artemisWorld

    init {
        gameWorld.artemisWorld.inject(this, true)
    }


    fun createCard(name: String, color: Color, x: Float, y: Float, width: Float, height: Float): Int {

        log.debug { "Generating Card $name" }
        val entity = artemisWorld.create()

        mTransform.create(entity).apply {
            velocity = Vector2(x, y)
        }

        mCard.create(entity).apply {
            cardName = name
            cardColor = color
        }

        mPosition.create(entity).apply {
            this.x = x
            this.y = y
        }

        mTextureReference.create(entity).apply {
            path = "TODO"
        }

        mRectangle.create(entity).apply {
            this.width = width
            this.height = height
        }

        mNetworkComponent.create(entity)


        return entity
//
//        val scaleFactor = 0.7f
//        return engine.createEntity().apply {
//            add(TextureComponent(cardTexture))
//            add(TransformComponent(Vector2(x, y)))
//            add(RevealableComponent())
//            add(StateComponent())
//            add(TeamComponent(color))
//            add(NameComponent(name))
//            add(IDComponent(id))
//            add(
//                RectangleComponent(
//                    cardTexture!!.regionWidth.toFloat() * scaleFactor,
//                    cardTexture!!.regionHeight.toFloat() * scaleFactor
//                )
//            )
//            add(
//                ClickableComponent(
//                    cardTexture!!.regionWidth.toFloat() * scaleFactor,
//                    cardTexture!!.regionHeight.toFloat() * scaleFactor
//                )
//            )
//        }
    }

    fun createPlayer(playerName: String, connectionId: Int): Int {
        val entity = artemisWorld.create()

        mPlayer.create(entity).apply {
            connectionPlayerId = connectionId
            name = playerName
        }

        return entity
    }
}