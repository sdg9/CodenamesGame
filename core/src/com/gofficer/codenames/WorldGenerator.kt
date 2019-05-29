package com.gofficer.codenames

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.screens.play.PlayScreen
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.utils.vanillaWordList

class WorldGenerator(private val world: GameWorld) {

    init {

    }

    fun generateGame() {
        generateXUniqueCards(25)
    }

    private fun getRandomArbitrary(min: Int, max: Int): Int {
        return Math.floor(Math.random() * (max - min)).toInt() + min
    }

    private fun generateXUniqueCards(count: Int) {
        println("Getting $count unique cards")
//        val cards = mutableListOf<PlayScreen.ECSCard>()
        val cards = mutableListOf<String>()
        var attempts = 0

        val isBlueFirst = Math.random() > 0.5
        val totalBlue = if (isBlueFirst) 9 else 8
        val totalRed = if (!isBlueFirst) 9 else 8
//        val types = mutableListOf<String>()
        val types = mutableListOf<Color>()

        for (i in 1..totalBlue) {
            types.add(Color.BLUE)
        }
        for (i in 1..totalRed) {
            types.add(Color.RED)
        }
        types.add(Color.BLACK);
        while (types.size < 25) {
            types.add(Color.BROWN)
        }
        val shuffledTypes = types.shuffled()

        while (cards.size < count) {
//        println("Adding more: ${cards.size}")
            val random = getRandomArbitrary(0, vanillaWordList.size)
//        println("Random: $random")
            val exists = cards.any { it == vanillaWordList[random] }
            if (!exists) {
                cards.add(vanillaWordList[random])
//                cards.add(
//                    PlayScreen.ECSCard(
//                        cards.size + 1,
//                        vanillaWordList.get(random),
//                        shuffledTypes.get(cards.size),
//                        false
//                    )
//                )
            }
            attempts += 1
            if (attempts > 200) {
                break
            }
        }

        for (i in 0..4) {
            for (j in 0..4) {
                val id = i + j * 5
                val card = cards[id]
                val color = types[id]
                // TODO consider passing row & col instead of x & y, let client resolve x/y
//            engine.addEntity(createCardAtCoordinate(engine, card.text, card.type, i, j, id, cardTexture))
//                    val x = 0f + row * GameConfig.WORLD_WIDTH / 6 + width / 2
//                val y = GameConfig.WORLD_HEIGHT - height - ((column + 1) * GameConfig.WORLD_HEIGHT / 6)
                world.entityFactory.createCard(card, color, 0f, 0f)
            }
        }
    }
}