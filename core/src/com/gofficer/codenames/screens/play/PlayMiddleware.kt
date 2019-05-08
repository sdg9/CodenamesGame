package com.gofficer.codenames.screens.play

import com.gofficer.codenames.models.AddCard
import com.gofficer.codenames.models.Card
import com.gofficer.codenames.models.CardType
import gofficer.codenames.game.GameState
import gofficer.codenames.game.ResetGame
import gofficer.codenames.game.SetupGame
import redux.api.Dispatcher
import redux.api.Store
import redux.api.enhancer.Middleware
import java.util.*

val setupGameMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->

    val action = next.dispatch(action)
    if (action is SetupGame) {
        store.dispatch(ResetGame())
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
        for (i in 1..25) {
            store.dispatch(AddCard(Card(i, "test$i", shuffledList[i-1])))
        }
    }
    action
}
