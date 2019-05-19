package com.gofficer.codenames.redux.models

import java.util.*

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
