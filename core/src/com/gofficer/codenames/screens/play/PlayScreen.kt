package com.gofficer.codenames.screens.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ScreenAdapter
import com.gofficer.codenames.CodenamesGame
import com.gofficer.codenames.models.AddCard
import com.gofficer.codenames.models.Card
import com.gofficer.codenames.models.CardType
import com.gofficer.codenames.utils.add
import com.gofficer.codenames.utils.logger
import gofficer.codenames.game.ResetGame
import java.util.*

class PlayScreen(val game: CodenamesGame) : ScreenAdapter() {

    companion object {
        @JvmStatic
        private val log = logger<PlayScreen>()
    }

    private val assetManager = game.assetManager
    private lateinit var renderer: PlayRenderer

    override fun show() {
        log.debug("show")
        renderer = PlayRenderer(game.font24, assetManager, game.store)

        setupGame()
        renderer.show()
    }

    override fun render(delta: Float) {
//        controller.update(delta)
        renderer.render(delta)


        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            hide()
            show()
        }
    }

    override fun resize(width: Int, height: Int) {
        log.debug("resize")
        renderer.resize(width, height)
    }

    override fun hide() {
        log.debug("hide")
        dispose()
    }

    override fun dispose() {
        log.debug("dispose")
        renderer.dispose()
    }

    private fun setupGame() {
        // Reset game back to vanilla
        game.store.dispatch(ResetGame())

        // Determine who goes first, red or blue
        val isBlueFirst = Random().nextBoolean()
        val types: MutableList<CardType> = mutableListOf()
        val totalBlue = if (isBlueFirst) 9 else 8
        val totalRed = if (!isBlueFirst) 9 else 8
        // Add appropriate number of color types
        for (i in 1..totalBlue) {
            types.add(CardType.BLUE)
        }
        for (i in 1..totalRed) {
            types.add(CardType.RED)
        }
        types.add(CardType.DOUBLE_AGENT)
        for (i in 1..(25 - types.size)) {
            types.add(CardType.BYSTANDER)
        }
        // Shuffle colors
        val shuffledList = types.shuffled()

        //Apply colors as cards are added
        // TODO: Implement dynamic card text, not allowing for duplicates
        for (i in 1..25) {
            game.store.dispatch(AddCard(Card(i, "test$i", shuffledList[i-1])))
        }
    }
}