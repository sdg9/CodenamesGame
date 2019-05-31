package com.gofficer.codenames.network.client

import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.network.interfaces.DefaultNotificationProcessor
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.network.notification.RemoveEntity
import com.gofficer.codenames.systems.client.ClientNetworkSystemOld
import ktx.log.logger

class GameNotificationProcessor : DefaultNotificationProcessor() {
    companion object {
        val log = logger<ClientNetworkSystemOld>()
    }
    override fun processNotification(entityUpdate: EntityUpdate) {
        log.debug { "Received an entity update $entityUpdate" }
//        if (!WorldManager.entityExsists(entityUpdate.entityId)) {
//            Log.info("Network entity doesn't exist: " + entityUpdate.entityId + ". So we create it")
//            val newEntity = GameScreen.getWorld().createEntity()
//            WorldManager.registerEntity(entityUpdate.entityId, newEntity.getId())
//            addComponentsToEntity(newEntity, entityUpdate)
//            if (E(newEntity).hasFocused()) {
//                Log.info("New focused player: " + newEntity.getId())
//                GameScreen.setPlayer(newEntity.getId())
//            }
//        } else {
//            Log.info("Network entity exists: " + entityUpdate.entityId + ". Updating")
//            updateEntity(entityUpdate)
//        }
    }

    override fun processNotification(removeEntity: RemoveEntity) {
        Log.info("Unregistering entity: " + removeEntity.entityId)
//        WorldManager.unregisterEntity(removeEntity.entityId)
    }

}