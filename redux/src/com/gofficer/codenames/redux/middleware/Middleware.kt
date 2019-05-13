package com.gofficer.codenames.redux.middleware

import com.gofficer.codenames.redux.actions.*
import com.gofficer.codenames.redux.models.AddCard
import com.gofficer.codenames.redux.models.Card
import com.gofficer.codenames.redux.utils.getXUniqueCards
import gofficer.codenames.redux.game.GameState
import redux.api.Dispatcher
import redux.api.Store
import redux.api.enhancer.Middleware
import java.util.*

val loggingMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
    println("Action => $action")
    next.dispatch(action)
    println("New state => ${store.state}")
}

val validActionMiddleware = Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
    if (action !is Action) {
        println("Only allow action objects")
        null
    } else {
        next.dispatch(action)
        action
    }
}

fun setupGameMiddleware(isClient: () -> Boolean): Middleware<GameState> {
    // Confirmed the isClient check works!
    return  Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
        if (isClient()) {
            println("Setting up game but only a client")
//        if (client != null) {
            // don't do any of this if connected to server
            null
        } else {
            println("Setting up game no server")

            val action = next.dispatch(action)
            if (action is SetupGame) {
//                store.dispatch(ResetGame())
//                val cards = getXUniqueCards(25)
//                action.cards = getXUniqueCards(25)
//                val isBlueFirst = Random().nextBoolean()
                store.dispatch(SetupCards(getXUniqueCards(25)))
////        val types: MutableList<CardType> = mutableListOf()
//                val types: MutableList<String> = mutableListOf()
//                val totalBlue = if (isBlueFirst) 9 else 8
//                val totalRed = if (!isBlueFirst) 9 else 8
//                // Add appropriate number of color types
//                for (i in 1..totalBlue) {
////            types.add(CardType.BLUE)
//                    types.add("BLUE")
//                }
//                for (i in 1..totalRed) {
////            types.add(CardType.RED)
//                    types.add("RED")
//                }
////        types.add(CardType.DOUBLE_AGENT)
//                types.add("DOUBLE_AGENT")
//                for (i in 1..(25 - types.size)) {
////            types.add(CardType.BYSTANDER)
//                    types.add("BYSTANDER")
//                }
//                // Shuffle colors
//                val shuffledList = types.shuffled()
//                for (i in 1..25) {
//                    store.dispatch(AddCard(Card(i, "test$i", shuffledList[i - 1])))
//                }
            }

            action
        }
    }
}

//
//val setupGameMiddleware = { clinet: Any? ->
//    Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
//
//        if (client != null) {
//            // don't do any of this if connected to server
//            null
//        } else {
//
//            val action = next.dispatch(action)
//            if (action is SetupGame) {
//                store.dispatch(ResetGame())
//                val isBlueFirst = Random().nextBoolean()
////        val types: MutableList<CardType> = mutableListOf()
//                val types: MutableList<String> = mutableListOf()
//                val totalBlue = if (isBlueFirst) 9 else 8
//                val totalRed = if (!isBlueFirst) 9 else 8
//                // Add appropriate number of color types
//                for (i in 1..totalBlue) {
////            types.add(CardType.BLUE)
//                    types.add("BLUE")
//                }
//                for (i in 1..totalRed) {
////            types.add(CardType.RED)
//                    types.add("RED")
//                }
////        types.add(CardType.DOUBLE_AGENT)
//                types.add("DOUBLE_AGENT")
//                for (i in 1..(25 - types.size)) {
////            types.add(CardType.BYSTANDER)
//                    types.add("BYSTANDER")
//                }
//                // Shuffle colors
//                val shuffledList = types.shuffled()
//                for (i in 1..25) {
//                    store.dispatch(AddCard(Card(i, "test$i", shuffledList[i - 1])))
//                }
//            }
//
//            action
//        }
//    }
//}


//val navigationMiddleware = { game: CodenamesGame ->
//    Middleware { store: Store<GameState>, next: Dispatcher, action: Any ->
//
//        //        println("Changing Screen $action")
//        if (action is ChangeScene) {
//            when (action.screenName) {
//                "MainMenu" -> {
////                    Gdx.app.postRunnable {
//                    println("Calling close on client")
//                    game.room?.leave()
//                    game.client?.close()
////                    game.client?.close()
////                    game.client = null
//                    game.screen = MainMenuScreen(game)
////                    }
//                }
//                "PlayOnline" -> {
//                    game.connectToServer {
//                        Gdx.app.postRunnable {
//                            println("Online play")
//                            game.screen = PlayScreen(game)
//                        }
//                    }
//                }
//                "Play" -> {
////                    Gdx.app.postRunnable {
//                    game.screen = PlayScreen(game)
////                    }
//                }
//                else -> null
//            }
//        } else {
//            next.dispatch(action)
//        }
//    }
//}