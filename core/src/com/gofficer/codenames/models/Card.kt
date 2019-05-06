package com.gofficer.codenames.models

import com.gofficer.codenames.reduxAction.CardPressed
import com.gofficer.redux.Action

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
    return when(action) {
        is CardPressed -> {
//            println("Revealing $action")
            copy(isRevealed = true)
        }
        else -> this
    }
}