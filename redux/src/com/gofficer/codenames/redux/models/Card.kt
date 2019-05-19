package com.gofficer.codenames.redux.models

import com.gofficer.codenames.redux.actions.CardPressed
import gofficer.codenames.redux.game.GameState
import redux.api.Reducer
import java.util.*


enum class CardType {
    RED, BLUE, BYSTANDER, DOUBLE_AGENT
}

data class Card(
        val id: Int,
        val text: String,
        val type: String,
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

//fun Card.reduce(action: com.gofficer.codenames.redux.Action): Card {
//    return when (action) {
//        is CardPressed -> copy(isRevealed = true)
//        else -> this
//    }
//}

val cardReduce = Reducer { state: GameState, action: Any ->
    when (action) {
        is CardPressed -> {
            println("PRocessing card pressed")
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


//data class CardPressed(val id: Int, val word: String, override var isFromServer: Boolean = false): NetworkAction {
//    override fun toJson(): String {
//        var gson = Gson()
//        var jsonString = gson.toJson(this)
//        return jsonString
//    }
//}
