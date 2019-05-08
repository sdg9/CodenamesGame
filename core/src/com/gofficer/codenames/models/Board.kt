package com.gofficer.codenames.models

import com.gofficer.codenames.Action
import com.gofficer.codenames.utils.add
import gofficer.codenames.game.GameState
import gofficer.codenames.game.ResetGame
import redux.api.Reducer

data class Board(
        val cards: List<Card> = listOf()) {

    val unguessedCards = cards.filter { !it.isRevealed }.map { it.id }
    val revealedCards = cards.filter { it.isRevealed }.map { it.id }


}

data class AddCard(val card: Card) : Action

val boardReduceSetup = Reducer { state: GameState, action: Any ->
    when (action) {
        is AddCard -> state.copy(cards = state.cards.add(action.card))
        else -> state
    }
}

//fun Board.reduceSetup(action: Action): Board {
//    return when (action) {
//        is AddCard -> copy(cards = cards.add(action.card))
//        else -> this
//    }
//}
//
//fun Board.reduceGameplay(action: Action): Board {
//    return when (action) {
//        is CardPressed -> {
//            val card = cards.getById(action.id)
//            if (card != null) {
//                val updated = card.reduce(action)
//                copy(cards = cards.update(updated))
//            } else {
//                this
//            }
//        }
//        else -> this
//    }
//}