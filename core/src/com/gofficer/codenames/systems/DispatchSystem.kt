package com.gofficer.codenames.systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.gofficer.codenames.CodenamesGame
import com.gofficer.codenames.assets.AssetDescriptors
import com.gofficer.codenames.assets.RegionNames
import com.gofficer.codenames.components.ActionComponent
import com.gofficer.codenames.components.CreateNewGame
import com.gofficer.codenames.components.RemoveComponent
import com.gofficer.codenames.components.TouchCardAction
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.createCard
import com.gofficer.codenames.screens.play.PlayScreen
import com.gofficer.codenames.utils.get
import com.gofficer.codenames.utils.vanillaWordList
import ktx.ashley.allOf
import ktx.log.debug

class DispatchSystem(val game: CodenamesGame) : IteratingSystem(allOf(ActionComponent::class).get(), Priority.DispatchSystem) {

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        debug { "Dispatching $entity" }

        val action = Mappers.action[entity].action

        when (action) {
            is TouchCardAction -> {
                debug { "Touched card ID ${action.id}"}
                println("Touched card ID ${action.id}")

            }
            is CreateNewGame -> {
                val assetManager = game.assetManager
                val gameplayAtlas = assetManager[AssetDescriptors.GAMEPLAY]
                val cardTexture = gameplayAtlas[RegionNames.CARD]
                createEntities(engine, cardTexture)
            }
            else -> {
                debug { "Unknown action"}

                println("Unknown action")
            }
        }
        entity?.add(RemoveComponent())

//        engine.removeEntity(entity)
    }
}

private val scaleFactor = 0.7f

private fun getRandomArbitrary(min: Int, max: Int): Int {
    return Math.floor(Math.random() * (max - min)).toInt() + min
}

private fun createEntities(
    engine: Engine,
    cardTexture: TextureRegion?
) {
    val cards = getXUniqueCards(25)
    for (i in 0..4) {
        for (j in 0..4) {
            val id = i + j * 5
            val card = cards.get(id)
            engine.addEntity(createCardAtCoordinate(engine, card.text, card.type, i, j, id, cardTexture))
        }
    }
}

private fun createCardAtCoordinate(
    engine: Engine,
    name: String,
    color: Color,
    row: Int,
    column: Int,
    id: Int,
    cardTexture: TextureRegion?
): Entity {

    val width = cardTexture!!.regionWidth.toFloat() * scaleFactor
    val height = cardTexture!!.regionHeight.toFloat() * scaleFactor

    val x = 0f + row * GameConfig.WORLD_WIDTH / 6 + width / 2
    val y = GameConfig.WORLD_HEIGHT - height - ((column + 1) * GameConfig.WORLD_HEIGHT / 6)

    return createCard(engine, name, color, x, y, id, cardTexture)

}

private fun getXUniqueCards(count: Int): List<PlayScreen.ECSCard> {
    println("Getting $count unique cards")
    val cards = mutableListOf<PlayScreen.ECSCard>()
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
        val exists = cards.any { it.text == vanillaWordList[random] }
        if (!exists) {
            cards.add(
                PlayScreen.ECSCard(
                    cards.size + 1,
                    vanillaWordList.get(random),
                    shuffledTypes.get(cards.size),
                    false
                )
            )
        }
        attempts += 1
        if (attempts > 200) {
            break
        }
    }
    return cards
}