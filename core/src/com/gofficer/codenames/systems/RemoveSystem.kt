package com.gofficer.codenames.systems

import com.artemis.annotations.One
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.gofficer.codenames.components.RemoveComponent

//
//import com.badlogic.ashley.core.Entity
//import com.badlogic.ashley.systems.IteratingSystem
//import com.gofficer.codenames.components.*
//import ktx.ashley.allOf
//import ktx.log.debug


//class RemoveSystem : IteratingSystem(allOf(RemoveComponent::class).get(), Priority.RemoveSystem) {
//
//    override fun processEntity(entity: Entity?, deltaTime: Float) {
//        debug { "Removing $entity" }
//        engine.removeEntity(entity)
//    }
//}

@Wire
@One(RemoveComponent::class)
class RemoveSystem: IteratingSystem() {
    override fun process(entityId: Int) {
        // TODO should all components be removed first?
        world.delete(entityId)
    }

}