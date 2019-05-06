package com.gofficer.codenames.models

import com.gofficer.codenames.utils.add
import com.gofficer.redux.Action

data class Board(
        val cards: List<Card> = listOf()) {

    val unguessedCards = cards.filter { !it.isRevealed }.map { it.id }
    val revealedCards = cards.filter { it.isRevealed }.map { it.id }
}

data class AddCard(val card: Card) : Action

fun Board.reduceSetup(action: Action): Board {
    return when (action) {
        is AddCard -> copy(cards = cards.add(action.card))
        else -> this
    }
}

fun Board.reduceGameplay(action: Action): Board {
    return when (action) {
        is CardPressed -> {
            val card = cards.getById(action.id)
            if (card != null) {
                val updated = card.reduce(action)
                copy(cards = cards.update(updated))
            } else {
                this
            }
        }
        else -> this
    }
}