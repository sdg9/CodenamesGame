package com.gofficer.codenames.network.shared

import com.artemis.Entity
import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.PositionComponent
import com.gofficer.codenames.network.client.ClientNotificationProcessor
import com.gofficer.codenames.network.interfaces.DefaultNotificationProcessor
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.network.notification.RemoveEntity
import com.gofficer.codenames.systems.SharedWorldManager
import ktx.log.logger


class SharedNotificationProcessor(val gameWorld: GameWorld) : DefaultNotificationProcessor() {
    companion object {
        val log = logger<SharedNotificationProcessor>()
    }


    override fun processNotification(entityUpdate: EntityUpdate) {
        ClientNotificationProcessor.log.debug { "Received an entity update $entityUpdate" }

//        val worldManager = gameWorld.artemisWorld.getSystem(SharedWorldManager::class.java)
        val worldManager = gameWorld.artemisWorld.getSystem(SharedWorldManager::class.java)

        if (!worldManager.entityExsists(entityUpdate.entityId)) {
            Log.info("Network entity doesn't exist: " + entityUpdate.entityId + ". So we create it")
            val newEntity = gameWorld.artemisWorld.createEntity()
            worldManager.registerEntity(entityUpdate.entityId, newEntity.getId())
            addComponentsToEntity(newEntity, entityUpdate)

            val stuff = newEntity.getComponent(PositionComponent::class.java)
            ClientNotificationProcessor.log.debug { "Created entity: $newEntity with $stuff" }
//            if (E(newEntity).hasFocused()) {
//                Log.info("New focused player: " + newEntity.getId())
//                GameScreen.setPlayer(newEntity.getId())
//            }
        } else {
            Log.info("Network entity exists: " + entityUpdate.entityId + ". Updating")
            updateEntity(entityUpdate)
        }
    }

    override fun processNotification(removeEntity: RemoveEntity) {
        Log.info("Unregistering entity: " + removeEntity.entityId)
//        WorldManager.unregisterEntity(removeEntity.entityId)
    }

    private fun addComponentsToEntity(newEntity: Entity, entityUpdate: EntityUpdate) {
        val edit = newEntity.edit()
        val components = entityUpdate.components
        if (components != null) {
            for (component in components) {
                Log.info("Adding component: $component")
                edit.add(component)
            }
        }
    }

    private fun updateEntity(entityUpdate: EntityUpdate) {
        val worldManager = gameWorld.artemisWorld.getSystem(SharedWorldManager::class.java)

        if (!worldManager.hasNetworkedEntity(entityUpdate.entityId)) {
            return
        }
        val entityId = worldManager.getNetworkedEntity(entityUpdate.entityId)

        if (entityId != null) {
            val entity = gameWorld.artemisWorld.getEntity(entityId)
            val edit = entity.edit()

            val components = entityUpdate.components
            if (components != null) {
                for (component in components) {
                    // this should replace if already exists
                    edit.add(component)
                    Log.info("Adding component: $component")
                }
            }
            val toRemove = entityUpdate.toRemove
            if (toRemove != null) {
                for (remove in toRemove) {
                    Log.info("Removing component: " + remove.simpleName)
                    edit.remove(remove)
                }
            }
        }

    }

}