package com.gofficer.codenames.models

import com.gofficer.codenames.Action
import com.gofficer.codenames.NetworkAction
import com.google.gson.Gson
import gofficer.codenames.game.GameState
import redux.api.Reducer


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

//fun Card.reduce(action: Action): Card {
//    return when (action) {
//        is CardPressed -> copy(isRevealed = true)
//        else -> this
//    }
//}

val cardReduce = Reducer { state: GameState, action: Any ->
    when (action) {
        is CardPressed -> {
            val card = state.cards.getById(action.id)
            if (card != null) {
//                val updated = card.reduce(action)
                val updated = card.copy(isRevealed = true)
                state.copy(cards = state.cards.update(updated))
            } else {
                state
            }
        }
        else -> state
    }
}


data class CardPressed(val id: Int, val word: String): NetworkAction {
//    override fun toJson(): String {
//        var gson = Gson()
//        var jsonString = gson.toJson(this)
//        return jsonString
//    }
}
