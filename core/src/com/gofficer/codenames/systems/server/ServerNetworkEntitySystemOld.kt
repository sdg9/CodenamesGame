package com.gofficer.codenames.systems.server

import com.artemis.Aspect
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.artemis.utils.IntBag
import com.gofficer.codenames.GameServer
import com.gofficer.codenames.components.CardComponent
import com.gofficer.codenames.components.PlayerComponent
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.systems.SpatialSystem
import com.gofficer.codenames.utils.mapper
import com.gofficer.codenames.utils.require
import com.gofficer.codenames.utils.system
import com.gofficer.codenames.utils.toMutableList
import ktx.log.logger

/**
 * system for keeping track of which entities should be on each
 * and every player/client, and which (we think/hope) are

 * entities should not be spawned manually, as this system
 * will take care of it, as well as notifying of destruction.

 * each tick it checks which entities should be added or removed
 * to the client's viewport region, compared to what we know/think
 * is already spawned on that client. and sends out appropriate net
 * commands

 */
@Wire(failOnNull = false)
class ServerNetworkEntitySystemOld(
    val gameServer: GameServer
) : IteratingSystem(Aspect.all()) {

    companion object {
        private val log = logger<ServerNetworkEntitySystemOld>()
        val INVALID_ENTITY_ID = -1
    }

    private val mCard by require<CardComponent>()
    private val mPlayer by mapper<PlayerComponent>()


    private val spatialSystem by system<SpatialSystem>()

    private val playerEntities = mutableListOf<PlayerEntities>()
//    private val mPlayer by mapper<PlayerComponent>()


    private val serverNetworkSystem by system<ServerNetworkSystemOld>()

    private inner class PlayerEntities(
        /**
         * entity id of the player whose viewport/list of spawned
         * entities it knows about
         */
        var playerEntityId: Int = INVALID_ENTITY_ID) {

        /**
         * we must know which entities the client has spawned, so we don't end up
         * re-sending them *all*, when his viewport moves a bit. so using this,
         * we know which ones the client has spawned, we know which ones need to be spawned
         *
         *
         * on client disconnection, all entities in here, associated with his player,
         * will be removed. this does not mean they will actually be removed from the world,
         * since this is just a "which entities does this client have in viewport"
         */
        internal var knownEntities = mutableListOf<Int>()
    }


    private inner class ConnectionListener : ServerNetworkSystemOld.NetworkServerConnectionListener {
        override fun playerDisconnected(playerEntityId: Int) {
            playerEntities.removeAll { playerEntities ->
                //remove all entity 'copies' for this player, since he's disconnecting
                playerEntities.playerEntityId == playerEntityId
            }
        }

        override fun playerConnected(playerEntityId: Int) {
            log.debug { "Player $playerEntityId connected"}
            playerEntities.add(PlayerEntities(playerEntityId))
        }
    }

    override fun initialize() {
        serverNetworkSystem.addConnectionListener(ConnectionListener())
    }

    override fun process(entityId: Int) {

//        log.debug { "Processing entity: $entityId"}

        val cCard = mCard.get(entityId)

        for (playerEntity in playerEntities) {

            val cPlayer = mPlayer.get(playerEntity.playerEntityId)

            val fill = IntBag()
            // TODO add viewport back?
//            spatialSystem.quadTree.get(fill, viewport.x.toFloat(), viewport.y.toFloat(), viewport.width.toFloat(),
//                viewport.height.toFloat())

            spatialSystem.quadTree.get(fill, 0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT)

            val entitiesInRegion = fill.toMutableList()

            val entitiesToSpawn = entitiesInRegion.filter { entityInRegion ->
                !playerEntity.knownEntities.contains(entityInRegion) &&
                        //hack ignore players for now, we don't spawn them via this mechanisms..it'd get hairy
                        //gotta rethink player spawn/destroying
                        !mPlayer.has(entityInRegion)
            }

            //list of entities we'll need to tell the client we no longer want him to have
            //remove from known, tell client he needs to delete that.
            val entitiesToDestroy = playerEntity.knownEntities.filter { knownEntity ->
                !entitiesInRegion.contains(knownEntity) && !mPlayer.has(knownEntity)
            }

            playerEntity.knownEntities.addAll(entitiesToSpawn)
            playerEntity.knownEntities.removeAll(entitiesToDestroy)


            maybeSendSpawn(entitiesToSpawn, cPlayer.connectionPlayerId)
            maybeSendDestroy(entitiesToDestroy, cPlayer.connectionPlayerId)
        }

    }

    private fun maybeSendDestroy(entitiesToDestroy: List<Int>, connectionPlayerId: Int) {
        if (entitiesToDestroy.isNotEmpty()) {
            log.debug { "sending DestroyMultipleEntities - list of entity id's: $entitiesToDestroy"}
            serverNetworkSystem.sendDestroyMultipleEntities(entitiesToDestroy,
                connectionPlayerId)
        }

    }

    private fun maybeSendSpawn(entitiesToSpawn: List<Int>, connectionPlayerId: Int) {
        if (entitiesToSpawn.isNotEmpty()) {
            log.debug { "sending SpawnMultipleEntities - list of entity id's: $entitiesToSpawn"}
            //send what is remaining...these are entities the client doesn't yet have, we send them in a batch
            serverNetworkSystem.sendSpawnMultipleEntities(entitiesToSpawn,
                connectionPlayerId)
        }
    }

}