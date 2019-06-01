package com.gofficer.codenames.components

import com.artemis.Component
import com.gofficer.codenames.utils.ExtendedComponent

class PositionComponent : Component(), ExtendedComponent<PositionComponent> {

    var x: Float = 0f
    var y: Float = 0f

    override fun copyFrom(other: PositionComponent) {
        this.x = other.x
        this.y = other.y
    }

    override fun canCombineWith(other: PositionComponent) = true
}