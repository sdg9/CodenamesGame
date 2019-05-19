package com.gofficer.codenames.redux.reducers

import com.gofficer.codenames.redux.actions.ResetGame
import com.gofficer.codenames.redux.actions.SetState
import com.gofficer.codenames.redux.actions.SetupCards
import com.gofficer.codenames.redux.models.Board
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
//        is ResetGame -> state.copy(
//            board = Board(),
//            lastPlayed = 0,
//            gameOver = false,
//            cards = listOf()
//        )
//        is SetState -> state.copy(
//            board = state.board,
//            lastPlayed = state.lastPlayed,
//            gameOver = state.gameOver,
//            cards = state.cards
//        )
        else -> state
    }
}
