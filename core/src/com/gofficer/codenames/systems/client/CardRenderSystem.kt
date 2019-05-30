package com.gofficer.codenames.systems.client

import com.artemis.Aspect
import com.artemis.BaseSystem
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
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


    private val font = BitmapFont()
    private val mTexture by mapper<TextureComponent>()
    private val mCard by mapper<CardComponent>()
    private val mTransform by mapper<TransformComponent>()

//    val backgroundAtlas: TextureAtlas = TextureAtlas(file("packed/backgrounds.atlas"))

    private val batch = SpriteBatch()

//    init {
//        private fun initSkin() {
//            skin.addRegions(uiSkinAtlas)
//            skin.add("default-font", client.font24)
//            skin.load(AssetPaths.UI_SKIN_JSON.toInternalFile())
//
//            Scene2DSkin.defaultSkin = skin
//        }
//    }

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

//        batch.projectionMatrix = camera.combined

        val layout = GlyphLayout(font, cCard.cardName)

        val fontX = cTransform.x + (100f - layout.width) / 2
        val fontY = cTransform.y + (150f - layout.height) / 3
//        val fontX = cTransform.x + (myRectangle.width - layout.width) / 2
//        val fontY = cTransform.y + (myRectangle.height - layout.height) / 3
        batch.use {
            batch.color = Color.BLUE
            batch.draw(cTexture.texture, cTransform.x, cTransform.y, 150f, 100f)
            font.draw(batch, cCard.cardName, fontX, fontY)
//            batch.color = if (isRevealed == true && !suppressColor) teamColor else Color.WHITE
            batch.color = Color.WHITE
        }
    }


}