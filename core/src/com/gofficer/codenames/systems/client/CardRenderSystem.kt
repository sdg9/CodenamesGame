package com.gofficer.codenames.systems.client

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.*
import com.gofficer.codenames.utils.RenderSystemMarker
import com.gofficer.codenames.utils.mapper
import com.gofficer.codenames.utils.use
import ktx.log.logger

@Wire
@All(TextureRenderableComponent::class, PositionComponent::class)
class CardRenderSystem(private val gameWorld: GameWorld) : IteratingSystem() , RenderSystemMarker {
    companion object {
        val log = logger<CardRenderSystem>()
    }


    private val font = BitmapFont()
    private val mCard by mapper<CardComponent>()
    private lateinit var mTextureRenderable: ComponentMapper<TextureRenderableComponent>
    private lateinit var mPosition: ComponentMapper<PositionComponent>
    private lateinit var mRectangle: ComponentMapper<RectangleComponent>
    private val mRevealed by mapper<RevealedComponent>()

    private val batch = SpriteBatch()


    override fun process(entityId: Int) {

        val cCard = mCard.get(entityId)
        val position = mPosition.get(entityId)
        val rectangle = mRectangle.get(entityId)
        val cTextureRenderable = mTextureRenderable.get(entityId)

        val layout = GlyphLayout(font, cCard.cardName)

        val fontX = position.x + (rectangle.width - layout.width) / 2
        val fontY = position.y + (rectangle.height - layout.height) / 3
//        clearScreen()
//        batch.projectionMatrix = camera.combined
        val isRevealed = mRevealed.has(entityId)

//        log.debug { "Texture: ${cTextureRenderable.textureRegion}" }
        batch.use {
//            batch.color = cCard.cardColor
//            batch.color = if (isRevealed == true && !suppressColor) cCard.cardColor else Color.WHITE
            batch.color = if (isRevealed == true) cCard.cardColor else Color.WHITE
            batch.draw(cTextureRenderable.textureRegion, position.x, position.y, rectangle.width, rectangle.height)
            font.draw(batch, cCard.cardName, fontX, fontY)
//            batch.color = if (isRevealed == true && !suppressColor) teamColor else Color.WHITE
            batch.color = Color.WHITE
        }
    }


}