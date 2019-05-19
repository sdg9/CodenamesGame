package gofficer.codenames.redux.game

import com.gofficer.codenames.redux.models.*

data class GameState(
    val gameOver: Boolean = false,
    val cards: List<Card> = listOf()
)