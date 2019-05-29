package com.gofficer.codenames.components

import com.artemis.Component
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.utils.DoNotCopy
import com.gofficer.codenames.utils.ExtendedComponent


class TransformComponent : Component(), ExtendedComponent<TransformComponent> {
    @DoNotCopy
    var velocity = Vector2()

    override fun copyFrom(other: TransformComponent) {
        velocity.set(other.velocity)
    }

    override fun canCombineWith(other: TransformComponent) = true
}