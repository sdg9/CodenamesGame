package com.gofficer.codenames.systems.server

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.gofficer.codenames.components.NetworkComponent
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.log.info

class ServerNetworkSystem : IteratingSystem(allOf(NetworkComponent::class).get()) {

    override fun processEntity(entity: Entity?, deltaTime: Float) {
//        serverNetworkSystem.sentEntity(entity)
//
//        info { "Send $entity over the wire"}
//        entity?.remove<NetworkComponent>()
    }

}