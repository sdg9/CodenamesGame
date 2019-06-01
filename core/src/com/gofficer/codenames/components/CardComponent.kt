package com.gofficer.codenames.components

import com.artemis.Component
import com.badlogic.gdx.graphics.Color
import com.gofficer.codenames.utils.ExtendedComponent
import com.gofficer.codenames.utils.defaultCopyFrom

class CardComponent : Component(), ExtendedComponent<CardComponent> {

    var cardName: String = ""
    var cardColor: Color = Color.WHITE

    override fun copyFrom(other: CardComponent) {
        this.defaultCopyFrom(other)
    }

    override fun canCombineWith(other: CardComponent): Boolean {
        return this.cardName == other.cardName
    }

}