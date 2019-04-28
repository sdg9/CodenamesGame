package gofficer.codenames.game

data class GameState(
//        val board1: Board,
//        val board2: Board,
        val red: Float = 0f,
        val green: Float = 0f,
        val blue: Float = 0f,
        val lastPlayed: Int = 0,
        val gameOver: Boolean = false) {

    fun hasBoardById(id: Int): Boolean {
        return when(id) {
//            board1.id, board2.id -> true
            else -> false
        }
    }

//    fun boardById(id: Int): Board {
//        return when (id) {
//            board1.id -> board1
//            board2.id -> board2
//            else -> throw RuntimeException("No such board with id: $id")
//        }
//    }
//
//    fun updateBoard(board: Board): GameState {
//        return when(board.id) {
//            board1.id -> copy(board1 = board)
//            board2.id -> copy(board2 = board)
//            else -> this
//        }
//    }
}