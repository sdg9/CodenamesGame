package com.gofficer.codenames.network.server

import com.gofficer.codenames.GameServer
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.network.interfaces.DefaultNotificationProcessor
import com.gofficer.codenames.network.notification.EntityUpdate
import ktx.log.logger

class ServerNotificationProcessor(val world: GameWorld) : DefaultNotificationProcessor() {

    companion object {
        val log = logger<ServerNotificationProcessor>()
    }

    override fun processNotification(entityUpdate: EntityUpdate) {
        log.debug { "Processing entity update" }
//        server.networkManager.notifyToNearEntities(entityUpdate.entityId, entityUpdate)
    }

}
