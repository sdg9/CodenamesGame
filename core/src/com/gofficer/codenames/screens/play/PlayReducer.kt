package com.gofficer.codenames.screens.play

import com.gofficer.codenames.game.reduxAction.CardPressed
import com.gofficer.redux.Action
import com.gofficer.redux.reduceChildState
import gofficer.codenames.game.GameState

fun PlayState.reducePlay(action: Action): PlayState {
    return when(action) {
//        is CardPressed -> copy(guessed = action.word)
////        is CardPressed -> reduceChildState(this, playState, action, PlayState::red)copy(guessed = action.word)
        else -> this
    }
//    return this
}
