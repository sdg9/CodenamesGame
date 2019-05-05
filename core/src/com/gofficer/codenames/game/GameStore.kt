package com.gofficer.codenames.game

import com.gofficer.codenames.reduceGameplay
import com.gofficer.codenames.reduceSetup
import com.gofficer.codenames.screens.play.reducePlay
import com.gofficer.codenames.utils.logger
import com.gofficer.redux.Action
import com.gofficer.redux.Dispatch
import com.gofficer.redux.Next
import com.gofficer.redux.SimpleStore
import gofficer.codenames.game.GameState


class Gamestore(initialSate: GameState): SimpleStore<GameState>(
        initialState = initialSate,
        middlewares = listOf(
//                ::stateValidityMiddleware,
//                ::gameSetupMiddleware,
//                ::moveMiddleware,
//                ::hitMiddleware,
//                ::destroyMiddleware,
//                ::lostMiddleware,
                ::logMiddleware
//                logMiddleware
        ),
        reducers = listOf(
//                GameState::reduceSetup,
//                GameState::reduceGameplay,
                GameState::reducePlay
        )
) {

        companion object {
                @JvmStatic
                private val log = logger<Gamestore>()
        }

}

fun logMiddleware(gameState: GameState, action: Action, dispatch: Dispatch, next: Next<GameState>): Action {
  println("Dispatching action: $action")
  return next(gameState, action, dispatch)
}
