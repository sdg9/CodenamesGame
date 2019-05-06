package gofficer.codenames.game

import com.gofficer.codenames.models.*
import com.gofficer.codenames.reduxAction.CardPressed
import com.gofficer.codenames.utils.add
import com.gofficer.redux.Action
import com.gofficer.redux.reduceChildState


data class GameState(

        val board: Board = Board(),
        val lastPlayed: Int = 0,
        val gameOver: Boolean = false,
        val guessed: String? = null,
        val cards: List<Card> = listOf()
        ) {


}

fun GameState.reduceGameplay(action: Action): GameState {
    return when(action) {
//        is ChangeColor -> copy(red = action.red, blue = action.blue, green = action.green)
//        is CardPressed -> copy(guessed = action.word)

//        is AddCard -> copy(guessed = action.card.text)
        is AddCard -> reduceChildState(this, board, action, Board::reduceSetup, { state, boardNew -> copy(board = boardNew) } )
        is CardPressed -> reduceChildState(this, board, action, Board::reduceGameplay, { state, boardNew -> copy(board = boardNew) })
//        is CardPressed -> reduceChildState(this, board, action, PlayState::reducePlay, {state, board -> state.updateBoard(board)})
//        is CardPressed -> reduceChildState(this, board, action, Board.reduceSetup)
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

fun GameState.reduceSetup(action: Action): GameState {
    return when(action) {
        is AddCard -> reduceChildState(this, board, action, Board::reduceSetup)
        else -> this
    }
}