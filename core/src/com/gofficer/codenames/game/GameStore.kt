package com.gofficer.codenames.game

import com.gofficer.codenames.reduceGameplay
import com.gofficer.codenames.reduceSetup
import com.gofficer.codenames.redux.SimpleStore
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
//                ::switcherMiddleware
        ),
        reducers = listOf(
                GameState::reduceSetup,
                GameState::reduceGameplay
        )
)