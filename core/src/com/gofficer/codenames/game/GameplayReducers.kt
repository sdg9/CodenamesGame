package com.gofficer.codenames
import com.gofficer.codenames.game.reduxAction.CardPressed
import com.gofficer.codenames.game.reduxAction.ChangeColor
import com.gofficer.codenames.screens.play.Board
import com.gofficer.codenames.screens.play.PlayState
import com.gofficer.codenames.screens.play.reducePlay
//import com.gofficer.codenames.screens.play.reducePlay
import com.gofficer.redux.Action
import com.gofficer.redux.Reducer
import com.gofficer.redux.reduceChildState
import gofficer.codenames.game.GameState

fun GameState.reduceSetup(action: Action): GameState {
    return when(action) {
//        is AddShip -> reduceChildState(this, boardById(action.offense), action, Board::reduceSetup, { state, board -> state.updateBoard(board) })
        else -> this
    }
}

fun GameState.reduceGameplay(action: Action): GameState {
    return when(action) {
        is ChangeColor -> copy(red = action.red, blue = action.blue, green = action.green)
        is CardPressed -> copy(guessed = action.word)
//        is CardPressed -> reduceChildState(this, board, action, PlayState::reducePlay, {state, board -> state.updateBoard(board)})
        is CardPressed -> reduceChildState(this, board, action, Board.reduceSetup)
//        is Chang eColor -> {
//            Gdx.app.log("GamePlay Reducer", action.toString());
//            copy(red = action.red)
//            copy(green = action.green)
//            copy(blue = action.blue)
//        }
//        is GeneratedAction -> {
//            val offense = boardById(action.offense).reduceOffense(action)
//            val defense = boardById(action.defense).reduceDefense(action)
//            copy(board1 = whichBoard(board1, offense, defense),
//                    board2 = whichBoard(board2, offense, defense),
//                    gameOver = action is GeneratedAction.DefinitiveAction.LostGame)
//
//        }
//        is SwitchAction -> {
//            reduceGameplay(action.last).copy(lastPlayed = action.offense)
//        }
        else -> this
    }
}

fun<State, Child> reduceChildState(
        state: State,
        child: Child,
        action: Action,
        reducer: Reducer<Child>,
        onReduced: (State, Child) -> State): State {

    val reduced = reducer.invoke(child, action)
    if (reduced === child) {
        return state
    }

    return onReduced(state, reduced)
}
//
//private fun whichBoard(board: Board, offense: Board, defense: Board): Board {
//    return when(board.id) {
//        offense.id -> offense
//        defense.id -> defense
//        else -> board
//    }
//}