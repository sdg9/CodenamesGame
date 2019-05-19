package com.gofficer.codenames.redux.reducers

import com.gofficer.codenames.redux.actions.SetState
import com.gofficer.codenames.redux.actions.SetupCards
import com.gofficer.codenames.redux.actions.TouchCard
import com.gofficer.codenames.redux.models.getById
import com.gofficer.codenames.redux.models.update
import gofficer.codenames.redux.game.GameState
import redux.api.Reducer


val reduceGameSetup = Reducer { state: GameState, action: Any ->
    when (action) {
        is SetupCards -> {
            println("Setting up cards")
            state.copy(
                cards = action.cards
            )
        }
        is TouchCard -> {
            println("Touching card")
            val card = state.cards.getById(action.id)
            if (card != null && !card.isRevealed) {
                val updated = card.copy(isRevealed = true)
                state.copy(cards = state.cards.update(updated))
            } else {
                state
            }
        }
        is SetState -> {
            println("Processing set state")
            state.copy(
                gameOver = action.state.gameOver,
                cards = action.state.cards
            )
        }
        else -> {
            println("Action was unmatched ${action::class.simpleName}")
            state
        }
    }
}