package com.gofficer.codenames.actors

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.Gamestore
import com.gofficer.codenames.models.CardType
import com.gofficer.codenames.models.getById
import com.gofficer.redux.Unsubscribe
import com.gofficer.codenames.utils.logger


class Card(private var id: Int, private var cardText: String, assetManager: AssetManager, private val font: BitmapFont, store: Gamestore) : Actor() {

    companion object {
        @JvmStatic
        private val log = logger<Card>()
    }

    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    private val cardTexture = gameplayAtlas[RegionNames.CARD]
    private var textToUse: String = cardText
    private var tint: Color? = null

    private var unsubscribe: Unsubscribe? = null

    fun setCardText(text: String) {
        cardText = text
        textToUse = cardText
    }

    init {
        width = 260f / 1.5f
        height = 166f / 1.5f

//        setBounds(x, y, width, height)
        unsubscribe = store.subscribe { state, dispatch ->
            log.debug("Responding to state")
            val me = state.board.cards.getById(id)
            if (me != null && me.isRevealed) {
                tint = when (me.type) {
                    CardType.RED -> Color.RED
                    CardType.BLUE -> Color.BLUE
                    CardType.BYSTANDER -> Color.YELLOW
                    CardType.DOUBLE_AGENT -> Color(0f, 0f, 0f, 0.1f)
                }
            }
        }
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        if (batch == null) {
            return
        }

        font.color = Color.WHITE

        if (tint != null) {
            batch.color = tint
        }


        batch.draw(cardTexture, x, y, originX, originY,
                width, height, 1f, 1f, rotation)
        // draw text below image

        font.draw(batch, textToUse, x + width / 7, y + height / 2.5f)


        batch.color = Color.WHITE
    }

    // TODO: Determine which lifecycle method is ideal (if any) for cleaning up of redux subscription
    override fun clear() {
        log.debug("clear")
        unsubscribe?.invoke()
        super.clear()
    }

    override fun remove(): Boolean {
        log.debug("remove")
        unsubscribe?.invoke()
        return super.remove()
    }
}