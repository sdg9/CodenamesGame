package com.gofficer.codenames.systems.server

import com.gofficer.codenames.GameServer
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.network.notification.RemoveEntity
import com.gofficer.codenames.network.server.ServerRequestProcessor
import com.gofficer.codenames.utils.system
import ktx.log.logger

class WorldManager(val world: GameWorld, server: GameServer) : DefaultManager(server) {
    companion object {
        val log = logger<WorldManager>()
    }
//    private val networkManager by system<NetworkManager>()
    private lateinit var networkManager: NetworkManager

    fun sendEntityRemove(user: Int, entity: Int) {
        sendEntityUpdate(user, RemoveEntity(entity))
    }

    fun sendEntityUpdate(user: Int, update: Any) {
        val userId = networkManager.getConnectionByPlayer(user)
        if (userId != null && networkManager.playerHasConnection(userId)) {
            log.debug { "Actually sending to $userId"}
            networkManager.sendTo(userId, update)
        } else {
            log.error { "No connection for player $user"}
        }
    }

}