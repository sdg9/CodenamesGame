package com.gofficer.codenames.network.client

import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.network.interfaces.DefaultNotificationProcessor
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.network.notification.RemoveEntity
import com.gofficer.codenames.network.shared.SharedNotificationProcessor
import ktx.log.logger

class ClientNotificationProcessor(val gameWorld: GameWorld) : DefaultNotificationProcessor() {
    companion object {
        val log = logger<ClientNotificationProcessor>()
    }

    private val sharedNotificationProcessor = SharedNotificationProcessor(gameWorld)

    override fun processNotification(entityUpdate: EntityUpdate) {
        sharedNotificationProcessor.processNotification(entityUpdate)
    }

    override fun processNotification(removeEntity: RemoveEntity) {
        Log.info("Unregistering entity: " + removeEntity.entityId)
//        WorldManager.unregisterEntity(removeEntity.entityId)
    }

}