package gofficer.codenames.game

import com.gofficer.codenames.models.*
import com.gofficer.codenames.utils.add
import com.gofficer.redux.Action
import com.gofficer.redux.reduceChildState


data class GameState(
        val board: Board = Board(),
        val lastPlayed: Int = 0,
        val gameOver: Boolean = false,
        val cards: List<Card> = listOf()
        )


fun GameState.reduceGameplay(action: Action): GameState {
    return when(action) {
        is ResetGame -> copy(
                board = Board(),
                lastPlayed = 0,
                gameOver = false,
                cards = listOf()
        )
        is AddCard -> reduceChildState(this, board, action, Board::reduceSetup, { state, boardNew -> copy(board = boardNew) } )
        is CardPressed -> reduceChildState(this, board, action, Board::reduceGameplay, { state, boardNew -> copy(board = boardNew) })
        else -> this
    }
}

fun GameState.reduceSetup(action: Action): GameState {
    return when(action) {
        is AddCard -> reduceChildState(this, board, action, Board::reduceSetup)
        else -> this
    }
}

class ResetGame : Action
class SetupGame : Action