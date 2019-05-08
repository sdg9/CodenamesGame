package com.gofficer.codenames






//import com.gofficer.codenames.models.AddCard
//import com.gofficer.codenames.models.Card
//import com.gofficer.codenames.models.CardType
//import com.gofficer.codenames.utils.logger
//import com.gofficer.redux.Action
//import com.gofficer.redux.Dispatch
//import com.gofficer.redux.Next
//import com.gofficer.redux.SimpleStore
//
//import com.gofficer.codenames.utils.add
//import gofficer.codenames.game.*
//import java.util.*
//
//
//class Gamestore(initialSate: GameState) : SimpleStore<GameState>(
//        initialState = initialSate,
//        middlewares = listOf(
////                ::stateValidityMiddleware,
////                ::gameSetupMiddleware,
////                ::moveMiddleware,
////                ::hitMiddleware,
////                ::destroyMiddleware,
////                ::lostMiddleware,
//
//                ::logMiddleware,
//                ::setupGameMiddleware
////                logMiddleware
//        ),
//        reducers = listOf(
//                GameState::reduceSetup,
//                GameState::reduceGameplay
////                GameState::reducePlay
////        PlayState::reducePlay
//        )
//) {
//
//    companion object {
//        @JvmStatic
//        private val log = logger<Gamestore>()
//    }
//
//}
//
//fun logMiddleware(gameState: GameState, action: Action, dispatch: Dispatch, next: Next<GameState>): Action {
//    println("Dispatching action: $action")
//
//    println("Before state: $gameState")
//    val action = next(gameState, action, dispatch)
//
//    println("Action result $action")
//    println("Next state: $gameState")
//
//    return action
//}
//
//fun setupGameMiddleware(gameState: GameState, action: Action, dispatch: Dispatch, next: Next<GameState>): Action {
//
//    // TODO: See if you can fix me, currently i update state to whatever I return last, and I MUST return something
//
//
//    // TODO: Doesn't appear as if a middleware can send off multiple dispatches
//    // TODO: How do i pipe through reset, then all the add?
////    val retValAction = next(gameState, action, dispatch)
////    var retValAction = next(gameState, action, dispatch)
////    if (action is SetupGame) {
////        val isBlueFirst = Random().nextBoolean()
////        val types: MutableList<CardType> = mutableListOf()
////        val totalBlue = if (isBlueFirst) 9 else 8
////        val totalRed = if (!isBlueFirst) 9 else 8
////        // Add appropriate number of color types
////        for (i in 1..totalBlue) {
////            types.add(CardType.BLUE)
////        }
////        for (i in 1..totalRed) {
////            types.add(CardType.RED)
////        }
////        types.add(CardType.DOUBLE_AGENT)
////        for (i in 1..(25 - types.size)) {
////            types.add(CardType.BYSTANDER)
////        }
////        // Shuffle colors
////        val shuffledList = types.shuffled()
////
////        //Apply colors as cards are added
////        // TODO: Implement dynamic card text, not allowing for duplicates
////        dispatch(ResetGame())
////        for (i in 1..25) {
////            dispatch(AddCard(Card(i, "test$i", shuffledList[i - 1])))
//////            next(gameState, AddCard(Card(i, "test$i", shuffledList[i - 1])), dispatch)
//////            println("RV $retVal")
////        }
////    }
//    return next(gameState, action, dispatch)
//
//}