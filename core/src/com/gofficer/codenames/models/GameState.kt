package gofficer.codenames.game

import com.gofficer.codenames.Action
import com.gofficer.codenames.models.*
import redux.api.Reducer


data class GameState(
        val board: Board = Board(),
        val lastPlayed: Int = 0,
        val gameOver: Boolean = false,
        val cards: List<Card> = listOf()
)


val reduceGameSetup = Reducer { state: GameState, action: Any ->
    when (action) {
        is ResetGame -> state.copy(
                board = Board(),
                lastPlayed = 0,
                gameOver = false,
                cards = listOf()
        )
        else -> state
    }
}

//fun GameState.reduceSetup(action: Action): GameState {
//    return when(action) {
//        is AddCard -> reduceChildState(this, board, action, Board::reduceSetup)
//        else -> this
//    }
//}

//val GameState.reduceGameplay = Reducer { state: GameState, action: Any ->
//
//    is ResetGame -> state.copy(
//    board = Board(),
//    lastPlayed = 0,
//    gameOver = false,
//    cards = listOf()
//    )
//
//    when (action) {
////        is Action1 -> state.copy(todos = state.todos + 1)
////        is Action2 -> state.copy(todos = state.todos - 1)
////        is Action3 -> state.copy(todos = state.todos - 1)
//        else -> state
//    }
////    when (action) {
////        "Inc" -> state.copy(todos = state.todos + 1)
////        "Dec" -> state.copy(todos = state.todos - 1)
////        else -> state
////    }
//}


//fun GameState.reduceGameplay(action: Action): GameState {
//    return when(action) {
//        is ResetGame -> copy(
//                board = Board(),
//                lastPlayed = 0,
//                gameOver = false,
//                cards = listOf()
//        )
//        is AddCard -> reduceChildState(this, board, action, Board::reduceSetup, { state, boardNew -> copy(board = boardNew) } )
//        is CardPressed -> reduceChildState(this, board, action, Board::reduceGameplay, { state, boardNew -> copy(board = boardNew) })
//        else -> this
//    }
//}
//
//fun GameState.reduceSetup(action: Action): GameState {
//    return when(action) {
//        is AddCard -> reduceChildState(this, board, action, Board::reduceSetup)
//        else -> this
//    }
//}
//

//val reduceGameSetup = Reducer { state: GameState, action: Any ->
//    when (action) {
////        is Action1 -> state.copy(todos = state.todos + 1)
////        is Action2 -> state.copy(todos = state.todos - 1)
////        is Action3 -> state.copy(todos = state.todos - 1)
//        else -> state
//    }
////    when (action) {
////        "Inc" -> state.copy(todos = state.todos + 1)
////        "Dec" -> state.copy(todos = state.todos - 1)
////        else -> state
////    }
//}


class ResetGame : Action

class SetupGame : Action