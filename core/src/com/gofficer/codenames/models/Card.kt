package com.gofficer.codenames.models

import com.gofficer.codenames.Action
import com.gofficer.codenames.utils.add
import gofficer.codenames.game.GameState
import redux.api.Reducer
import java.util.Collections.copy


enum class CardType {
    RED, BLUE, BYSTANDER, DOUBLE_AGENT
}

data class Card(
        val id: Int,
        val text: String,
        val type: CardType,
        val isRevealed: Boolean = false)

fun List<Card>.update(card: Card): List<Card> {
    var index = -1
    forEachIndexed { i, s ->
        if (s.id == card.id) {
            index = i
            return@forEachIndexed
        }
    }

    if (index != -1) {
        val mutable = ArrayList(this)
        mutable.removeAt(index)
        mutable.add(index, card)
        return mutable
    }

    return this
}

fun List<Card>.getById(id: Int): Card? {
    forEach {
        if (id == it.id) {
            return it
        }
    }

    return null
}

fun Card.reduce(action: Action): Card {
    return when (action) {
        is CardPressed -> copy(isRevealed = true)
        else -> this
    }
}

val cardReduce = Reducer { state: GameState, action: Any ->
    when (action) {
//        is AddCard -> state.copy(cards = state.cards.add(action.card))
//        is CardPressed -> state
//        is CardPressed -> reduceChildState(state, state.cards, action, childCardReducer, { state, cardsNew -> copy(state.cards = cardsNew) })
        is CardPressed -> {
            val card = state.cards.getById(action.id)
            if (card != null) {
                val updated = card.reduce(action)
                state.copy(cards = state.cards.update(updated))
            } else {
                state
            }
        }
        // TODO how do i make this work?
//        is CardPressed -> state.copy(state.cards[action.id].isRevealed = true)
        else -> state
    }
}


data class CardPressed(val id: Int, val word: String): Action

//val childCardReducer = Reducer { state: List<Card>, action: Any ->
//    when (action) {
//        is CardPressed -> {
//            val card = state.getById(action.id)
//            if (card != null) {
//                val updated = card.reduce(action)
//                copy(state = state.update(updated))
//            } else {
//                this
//            }
////            state.copy(state.cards[action.id].isRevealed = true)
//        }
//        else -> state
//    }
//}

//fun<State, Child> reduceChildState(
//        state: State,
//        child: Child,
//        action: Action,
//        reducer: Reducer<Child>,
//        onReduced: (State, Child) -> State): State {
//
//    val reduced = reducer.reduce(child, action)
//    if (reduced === child) {
//        return state
//    }
//
//    return onReduced(state, reduced)
//}