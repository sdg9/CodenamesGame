package com.gofficer.codenames

//import com.gofficer.codenames.reduceGameplay
//import com.gofficer.codenames.reduceSetup
//import com.gofficer.codenames.screens.play.PlayState
//import com.gofficer.codenames.screens.play.reducePlay
import com.gofficer.codenames.utils.logger
import com.gofficer.redux.Action
import com.gofficer.redux.Dispatch
import com.gofficer.redux.Next
import com.gofficer.redux.SimpleStore
import gofficer.codenames.game.GameState
import gofficer.codenames.game.reduceGameplay
import gofficer.codenames.game.reduceSetup


class Gamestore(initialSate: GameState) : SimpleStore<GameState>(
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
                GameState::reduceSetup,
                GameState::reduceGameplay
//                GameState::reducePlay
//        PlayState::reducePlay
        )
) {

    companion object {
        @JvmStatic
        private val log = logger<Gamestore>()
    }

}

fun logMiddleware(gameState: GameState, action: Action, dispatch: Dispatch, next: Next<GameState>): Action {
    println("Dispatching action: $action")

    println("Before state: $gameState")
    val action = next(gameState, action, dispatch)

    println("Next state: $gameState")

    return action
}
