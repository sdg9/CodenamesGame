package com.gofficer.codenames.network.server

import com.artemis.World
import com.badlogic.gdx.utils.TimeUtils
import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.GameServer
import com.gofficer.codenames.Network
import com.gofficer.codenames.network.interfaces.DefaultRequestProcessor
import com.gofficer.codenames.systems.server.NetworkManager
import com.gofficer.codenames.systems.server.ServerNetworkSystem
import com.gofficer.codenames.systems.server.WorldManager
import ktx.log.logger
import java.util.*
import com.artemis.utils.IntBag
import com.artemis.Aspect
import com.artemis.EntitySubscription
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.CardComponent
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.utils.forEach


/**
 * Every packet received from users will be processed here
 */
class ServerRequestProcessor(val world: GameWorld) : DefaultRequestProcessor() {

    companion object {
        val log = logger<ServerRequestProcessor>()
    }

    override fun processRequest(request: Network.Client.JoinRoomRequest, connectionId: Int) {
        log.debug { "Join room request received"}
        val player = Network.Shared.Player(connectionId, "TestName")
        // TODO if player had entity id, use that, otherwise connection id is fine
        world.artemisWorld.getSystem(NetworkManager::class.java).registerUserConnection(connectionId, connectionId)

    }

}
