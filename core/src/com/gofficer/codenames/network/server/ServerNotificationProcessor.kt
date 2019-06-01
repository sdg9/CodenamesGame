package com.gofficer.codenames.network.server

import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.network.interfaces.DefaultNotificationProcessor
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.network.shared.SharedNotificationProcessor
import ktx.log.logger

class ServerNotificationProcessor(val gameWorld: GameWorld) : DefaultNotificationProcessor() {

    companion object {
        val log = logger<ServerNotificationProcessor>()
    }

    private val sharedNotificationProcessor = SharedNotificationProcessor(gameWorld)


    override fun processNotification(entityUpdate: EntityUpdate) {
        sharedNotificationProcessor.processNotification(entityUpdate)
    }
}
