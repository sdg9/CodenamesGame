package com.gofficer.codenames.screens.play

import com.gofficer.codenames.game.reduxAction.CardPressed
import com.gofficer.codenames.game.reduxAction.ChangeColor
import com.gofficer.redux.Action
import gofficer.codenames.game.GameState

fun GameState.reducePlay(action: Action): GameState {
    return when(action) {
        is CardPressed -> copy(guessed = action.word)
        else -> this
    }
}
