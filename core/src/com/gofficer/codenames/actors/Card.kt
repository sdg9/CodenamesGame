package com.gofficer.codenames.actors

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.utils.Align
import com.gofficer.codenames.actions.FlipAction
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.models.CardType
import com.gofficer.codenames.models.getById
import com.gofficer.codenames.utils.logger
import gofficer.codenames.game.GameState
import redux.api.Store


class Card(private var id: Int, private var cardText: String, private var cardType: String, private var isRevealed: Boolean?, assetManager: AssetManager, private val font: BitmapFont, store: Store<GameState>) : Actor() {

    companion object {
        @JvmStatic
        private val log = logger<Card>()
    }

    private var subscription: Store.Subscription? = null
    private val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
    private val cardTexture = gameplayAtlas[RegionNames.CARD]
//    private var textToUse: String = cardText
    private var tint: Color? = if (isRevealed == true) getTint(cardType) else null
//    private var isRevealed: Boolean? = null
    private var store: Store<GameState> = store

    val duration = 0.5f
    private fun subscribeStore() {
        subscription?.unsubscribe()
        subscription = store.subscribe {
            // Before
            // TODO: Ideally we'd have before state, we don't with this implementation.
            //  Instead we can store what we care about locally and compare vs that

            // After
            val me = store.state.cards.getById(id)

            if (isRevealed != true && me?.isRevealed == true) {
                log.debug("I'm changing for first time to be revaled! $id")


                addAction(SequenceAction(
//                        isAnimating = true,
                        FlipAction.flipOut(x, width, duration / 2),
                        FlipAction.flipIn(x, width, duration / 2)
                ))
            }

            if (cardText != me?.text.toString()) {
                tint = null
                cardText = me?.text.toString()
                addAction(SequenceAction(
//                        isAnimating = true,

                        FlipAction.flipOut(x, width, duration / 2),
                        FlipAction.flipIn(x, width, duration / 2)
                ))
            }

            log.debug("Responding to state $me")
            if (me != null && me.isRevealed) {
                tint = when (me.type) {
                    "RED" -> Color.RED
                    "BLUE" -> Color.BLUE
                    "BYSTANDER" -> Color.YELLOW
                    "DOUBLE_AGENT" -> Color(0f, 0f, 0f, 0.1f)
//                    CardType.RED -> Color.RED
//                    CardType.BLUE -> Color.BLUE
//                    CardType.BYSTANDER -> Color.YELLOW
//                    CardType.DOUBLE_AGENT -> Color(0f, 0f, 0f, 0.1f)
                    else -> null
                }
            } else {
                tint = null
            }

            isRevealed = me?.isRevealed
        }
    }

    fun getTint(stringTint: String): Color? {
        return when (stringTint) {
            "RED" -> Color.RED
            "BLUE" -> Color.BLUE
            "BYSTANDER" -> Color.YELLOW
            "DOUBLE_AGENT" -> Color(0f, 0f, 0f, 0.1f)
            else -> null
        }
    }
//
//    fun setCardText(text: String) {
//        cardText = text
////        textToUse = cardText
//    }

    init {
        width = 260f / 1.5f
        height = 166f / 1.5f

//        this.store = store
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

        // TODO: Get this part of the actor so it animates as the card flips
//        log.debug("W $width")
//        font.draw(batch, textToUse, x + width / 7, y + height / 2.5f, width, Align.center, false)

        val isAnimating = actions.size > 0
        if (!isAnimating) {
            font.draw(batch, cardText, x , y + height / 2.5f, width, Align.center, true)
        }
//        font.draw(batch, textToUse, x + width / 7, y + height / 2.5f)


        batch.color = Color.WHITE
    }

//    override fun clear() {
//        log.debug("clear")
//        super.clear()
//    }
//
//    override fun remove(): Boolean {
//        log.debug("remove")
//        subscription?.unsubscribe()
//        return super.remove()
//    }

    override fun setStage(stage: Stage?) {
        super.setStage(stage)

        if (stage != null) {
//            log.debug("Actor added to stage")
            subscribeStore()
        } else {
//            log.debug("Actor removed from stage")
            subscription?.unsubscribe()
        }
    }
}