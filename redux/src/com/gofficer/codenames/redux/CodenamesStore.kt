package com.gofficer.codenames.redux

import com.gofficer.codenames.redux.actions.ResetGame
import com.gofficer.codenames.redux.actions.SetupCards
import com.gofficer.codenames.redux.models.Board
import gofficer.codenames.redux.game.GameState
import redux.api.Reducer
import redux.api.Store
import redux.api.enhancer.Middleware
import redux.applyMiddleware
import redux.combineReducers

fun createCodeNamesStore(initState: GameState, reducers: Array<Reducer<GameState>>, middleware: Array<Middleware<GameState>>): Store<GameState> {
    return redux.createStore(
        combineReducers(
            *reducers
        ),
        initState,
        applyMiddleware(
            *middleware
        )
    )
}

