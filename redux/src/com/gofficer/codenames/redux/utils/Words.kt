package com.gofficer.codenames.redux.utils

import com.gofficer.codenames.redux.data.vanillaWordList
import com.gofficer.codenames.redux.models.Card

fun getRandomArbitrary(min: Int, max: Int): Int {
    return Math.floor(Math.random() * (max - min)).toInt() + min
}

fun getXUniqueCards(count: Int): List<Card> {
    println("Getting $count unique cards")
    val cards = mutableListOf<Card>()
    var attempts = 0

    val isBlueFirst = Math.random() > 0.5
    val totalBlue = if (isBlueFirst) 9 else 8
    val totalRed = if (!isBlueFirst) 9 else 8
    val types = mutableListOf<String>()

    for (i in 1..totalBlue) {
        types.add("BLUE")
    }
    for (i in 1..totalRed) {
        types.add("RED")
    }
    types.add("DOUBLE_AGENT");
    while (types.size < 25) {
        types.add("BYSTANDER")
    }
    val shuffledTypes = types.shuffled()

    while (cards.size < count) {
//        println("Adding more: ${cards.size}")
        val random = getRandomArbitrary(0, vanillaWordList.size)
//        println("Random: $random")
        val exists = cards.any { it.text == vanillaWordList[random] }
        if (!exists) {
            cards.add(Card(cards.size + 1, vanillaWordList.get(random), shuffledTypes.get(cards.size), false))
        }
        attempts += 1
        if (attempts > 200) {
            break
        }
    }
    return cards
};