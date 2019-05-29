package com.gofficer.codenames.systems

//import com.badlogic.ashley.core.Entity
//import com.badlogic.ashley.systems.IteratingSystem
//import com.gofficer.codenames.components.ActionComponent
//import com.gofficer.codenames.components.RemoveComponent
//import com.gofficer.codenames.components.TouchCardAction
//import ktx.ashley.allOf
import ktx.log.debug

//
//class DispatchCleanupSystem : IteratingSystem(allOf(ActionComponent::class).get(), Priority.DispatchCleanupSystem) {
//
//    override fun processEntity(entity: Entity?, deltaTime: Float) {
//        entity?.add(RemoveComponent())
//    }
//}