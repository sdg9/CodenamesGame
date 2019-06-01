package com.gofficer.codenames.components

import com.artemis.Component
import com.gofficer.codenames.utils.ExtendedComponent
import com.gofficer.codenames.utils.defaultCopyFrom

class PlayerComponent : Component(), ExtendedComponent<PlayerComponent> {

    var name: String = ""
    var connectionPlayerId: Int = -1

    override fun copyFrom(other: PlayerComponent) {
        this.defaultCopyFrom(other)
    }

    override fun canCombineWith(other: PlayerComponent): Boolean {
        return this.connectionPlayerId == other.connectionPlayerId
    }

}