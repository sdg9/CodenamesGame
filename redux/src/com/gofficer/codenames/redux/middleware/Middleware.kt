package com.gofficer.codenames.redux.middleware

import com.gofficer.codenames.redux.actions.*
import com.gofficer.codenames.redux.utils.getXUniqueCards
import gofficer.codenames.redux.game.GameState
import redux.api.Dispatcher
import redux.api.Store
import redux.api.enhancer.Middleware

val loggingMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
    println("loggingMiddleware Action => $action")
    next.dispatch(action)
    println("New state => ${store.state}")
}

val validActionMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
    println("validActionMiddleware: $action")
    if (action !is BaseAction) {
        println("$action is not a base action, not allowing")
        null
    } else {
        next.dispatch(action)
        action
    }
}

fun setupGameMiddleware(isClient: () -> Boolean): Middleware<GameState> {
    // Confirmed the isClient check works!
    return  Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
        println("setupGameMiddleware: $action")
        if (isClient()) {
            println("Setting up game but only a client")
            next.dispatch(action)
            action
        } else {
            println("Setting up game no server")

            val action = next.dispatch(action)
            if (action is SetupGame || action is ResetGame) {
                store.dispatch(SetupCards(getXUniqueCards(25)))
            }
            next.dispatch(action)
            action
        }
    }
}
