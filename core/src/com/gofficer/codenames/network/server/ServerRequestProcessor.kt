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

//    private lateinit var serverNetworkSystem: ServerNetworkSystem
//    private lateinit var networkManager: NetworkManager
//    private lateinit var worldManager: WorldManager

    companion object {
        val log = logger<ServerRequestProcessor>()
    }

    override fun processRequest(request: Network.Client.JoinRoomRequest, connectionId: Int) {
//        super.processRequest(request, connectionId)
        log.debug { "Join room request received"}
//        serverNetworkSystem.
        val player = Network.Shared.Player(connectionId, "TestName")
        // TODO if player had entity id, use that, otherwise connection id is fine
        world.artemisWorld.getSystem(NetworkManager::class.java).registerUserConnection(connectionId, connectionId)
//        finisterra.getNetworkManager().registerUserConnection(player, connectionId)


        val subscription = world.artemisWorld.aspectSubscriptionManager.get(Aspect.all(CardComponent::class.java))
        val entityIds = subscription.entities


        log.debug { "Found IDs $entityIds"}
//        world.networkManager.sendTo(connectionId, En)
        entityIds.forEach {
            val update = EntityUpdate.EntityUpdateBuilder.of(it).build()
            log.debug{"Sending entity update for $update"}
            world.artemisWorld.getSystem(WorldManager::class.java).sendEntityUpdate(connectionId, update)
//            world.worldManager.sendEntityUpdate(connectionId, update)
        }
//        world.getSystem(WorldManager::class.java).sendEntityUpdate(entityId, update)
//        worldManager.world.artemisWorld.entityManager


//        networkManager.sendTo(connectionId, )
    }
//    private val world: World?
//        get() = server.world
//
//    private val networkManager: NetworkManager
//        get() = server.networkManager

//    private val mapManager: MapManager
//        get() = server.getMapManager()
//
//    private val worldManager: WorldManager
//        get() = server.getWorldManager()
//
//    private val itemManager: ItemManager
//        get() = server.getItemManager()
//
//    private val spellManager: SpellManager
//        get() = server.getSpellManager()
//
//    private fun getCombatSystem(type: AttackType): CombatSystem {
//        return if (type.equals(AttackType.PHYSICAL)) {
//            server.getCombatManager()
//        } else server.getCombatManager()
//        // TODO
//    }
//
//    private fun getArea(worldPos: WorldPos, range: Int /*impar*/): List<WorldPos> {
//        val positions = ArrayList<WorldPos>()
//        val i = range / 2
//        for (x in worldPos.x - i..worldPos.x + i) {
//            for (y in worldPos.y - i..worldPos.y + i) {
//                positions.add(WorldPos(x, y, worldPos.map))
//            }
//        }
//        return positions
//    }


}
