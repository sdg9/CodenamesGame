package com.gofficer.codenames.systems

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.gofficer.codenames.components.*
import ktx.ashley.allOf
import ktx.log.debug


class RemoveSystem : IteratingSystem(allOf(RemoveComponent::class).get(), 100) {

    override fun processEntity(entity: Entity?, deltaTime: Float) {
        debug { "Removing $entity" }
        engine.removeEntity(entity)
    }
}