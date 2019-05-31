package com.gofficer.codenames.systems.server

import com.gofficer.codenames.GameServer
import com.gofficer.codenames.network.notification.RemoveEntity
import com.gofficer.codenames.utils.system

class WorldManager(server: GameServer) : DefaultManager(server) {

//    private val networkManager by system<NetworkManager>()
    private lateinit var networkManager: NetworkManager

    internal fun sendEntityRemove(user: Int, entity: Int) {
        if (networkManager.playerHasConnection(user)) {
            val id = networkManager.getConnectionByPlayer(user)
            if (id != null) {
                networkManager.sendTo(id, RemoveEntity(entity))
            }
        }
    }
}