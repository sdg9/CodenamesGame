package com.gofficer.codenames.systems.client

import com.artemis.Aspect
import com.artemis.BaseSystem
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.CardComponent
import com.gofficer.codenames.components.TextureComponent
import com.gofficer.codenames.components.TransformComponent
import com.gofficer.codenames.utils.RenderSystemMarker
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.mapper
import com.gofficer.codenames.utils.use

@Wire
class CardRenderSystem(private val gameWorld: GameWorld, private val camera: Camera) : IteratingSystem(Aspect.one(CardComponent::class.java)) , RenderSystemMarker {

    private val mTexture by mapper<TextureComponent>()
    private val mCard by mapper<CardComponent>()
    private val mTransform by mapper<TransformComponent>()

//    val backgroundAtlas: TextureAtlas = TextureAtlas(file("packed/backgrounds.atlas"))

    private val batch = SpriteBatch()

//    override fun processSystem() {
////        clearScreen()
//        batch.projectionMatrix = camera.combined
//
//        renderCards()
//    }

    override fun process(entityId: Int) {

        val cCard = mCard.get(entityId)
        val cTransform = mTransform.get(entityId).velocity
        val cTexture = mTexture.get(entityId)
//        val entities = world.entities(allOf(SpriteComponent::class))
//        val entities = world.entities(allOf(SpriteComponent::class))
//        val entity = entities.get(i)

        batch.projectionMatrix = camera.combined
        batch.use {
            batch.color = Color.BLUE
            batch.draw(cTexture.texture, cTransform.x, cTransform.y, 100f, 100f)
//            font.draw(batch, myName, fontX, fontY)
//            batch.color = if (isRevealed == true && !suppressColor) teamColor else Color.WHITE
            batch.color = Color.WHITE
        }
    }


    private fun renderCards() {



//        val img = Mappers.texture[entity].texture
//        val position = Mappers.transform[entity].position
//        val isRevealed = Mappers.revealable[entity]?.isRevealed
//        val myRectangle = Mappers.rectangle[entity]
//        val myName = Mappers.name[entity].name
//        val teamColor = Mappers.teamMapper[entity].teamColor
//        val isAnimating: Boolean = entity?.has(Mappers.animation) ?: false
//        val suppressColor = Mappers.animation[entity]?.suppressColor == true
//
//        val layout = GlyphLayout(font, myName)
//        val fontX = position.x + (myRectangle.width - layout.width) / 2
//        val fontY = position.y + (myRectangle.height - layout.height) / 3
//        font.color = Color.BLACK
//
//        batch.use {
//            batch.color = if (isRevealed == true && !suppressColor) teamColor else Color.WHITE
//            batch.draw(img, position.x, position.y, myRectangle.width, myRectangle.height)
//            if (!isAnimating) {
//                font.draw(batch, myName, fontX, fontY)
//            }
//            batch.color = Color.WHITE
//        }
    }

}