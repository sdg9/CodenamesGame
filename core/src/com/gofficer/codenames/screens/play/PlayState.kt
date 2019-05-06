package com.gofficer.codenames.screens.play

import com.gofficer.redux.Action

enum class CardType {
    RED, BLUE, BYSTANDER, DOUBLE_AGENT
}

data class Card(val text: String, val type: CardType, val isRevealed: Boolean)

data class PlayState(val cards: List<Card> = listOf()) {
}

data class Board(
//        val width: Int,
//        val height: Int,
        val cards: List<Card> = listOf())


fun Board.reduceSetup(action: Action): Board {
    return when(action) {
//        is AddShip -> copy(ships = ships.add(action.ship))
        else -> this
    }
}