package com.gofficer.codenames.systems.server

import com.artemis.Aspect
import com.artemis.Component
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.artemis.utils.Bag
import com.artemis.utils.IntBag
import com.gofficer.codenames.GameServer
import com.gofficer.codenames.components.CardComponent
import com.gofficer.codenames.components.NetworkComponent
import com.gofficer.codenames.components.PlayerComponent
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.utils.mapper
import com.gofficer.codenames.utils.system
import com.gofficer.codenames.utils.toMutableList
import ktx.log.logger
import com.badlogic.gdx.utils.Array

@All(NetworkComponent::class)
class ServerNetworkEntitySystem() : IteratingSystem() {

    private val serverNetworkSystem by system<ServerNetworkSystem>()
    private val networkManager by system<NetworkManager>()
    private val playerEntities = mutableListOf<PlayerEntities>()

    private val mCard by mapper<CardComponent>()
    private val mNetwork by mapper<NetworkComponent>()
    private val mPlayer by mapper<PlayerComponent>()


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

    companion object {
        val log = logger<ServerNetworkEntitySystem>()
        val INVALID_ENTITY_ID = -1
    }


    override fun initialize() {
        serverNetworkSystem.addConnectionListener(ConnectionListener())
    }

    override fun process(entityId: Int) {
        for (playerEntity in playerEntities) {
            // TODO create player entities
//            val cPlayer = mPlayer.get(playerEntity.playerEntityId)
//            val fill = IntBag()
            val subscription = world.aspectSubscriptionManager.get(Aspect.all(NetworkComponent::class.java))
            val entitiesInRegion = subscription.entities.toMutableList()

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
//
            // TODO switch to player entity to house connection id
//            maybeSendSpawn(entitiesToSpawn, cPlayer.connectionPlayerId)
//            maybeSendDestroy(entitiesToDestroy, cPlayer.connectionPlayerId)
            maybeSendSpawn(entitiesToSpawn, playerEntity.playerEntityId)
            maybeSendDestroy(entitiesToDestroy, playerEntity.playerEntityId)
        }
    }

    private inner class ConnectionListener : ServerNetworkSystem.NetworkServerConnectionListener {
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

    private fun maybeSendDestroy(entitiesToDestroy: List<Int>, connectionPlayerId: Int) {
        if (entitiesToDestroy.isNotEmpty()) {
            log.debug { "sending DestroyMultipleEntities - list of entity id's: $entitiesToDestroy"}

//            e
//            networkManager.sendTo(connectionPlayerId, )
//            serverNetworkSystem.sendDestroyMultipleEntities(entitiesToDestroy,
//                connectionPlayerId)
        }

    }

    private fun maybeSendSpawn(entitiesToSpawn: List<Int>, connectionPlayerId: Int) {
        if (entitiesToSpawn.isNotEmpty()) {
            log.debug { "sending SpawnMultipleEntities - list of entity id's: $entitiesToSpawn"}
            //send what is remaining...these are entities the client doesn't yet have, we send them in a batch
            entitiesToSpawn.forEach {
                // TODO make sure attributes make it on to this request
                networkManager.sendTo(connectionPlayerId, EntityUpdate(it, serializeComponents(it)))
            }
//            serverNetworkSystem.sendSpawnMultipleEntities(entitiesToSpawn,
//                connectionPlayerId)
        }
    }

    private fun serializeComponents(entityId: Int): Array<Component> {
        val components = Bag<Component>()

        world.getEntity(entityId).getComponents(components)

        val copyComponents = Array<Component>()
        for (component in components) {
            assert(component != null) {
                "component in list of components for entity was null somehow. shouldn't be possible"
            }

            when (component) {
                is PlayerComponent -> {
                    //skip
                }
//                is SpriteComponent -> {
//                    //skip
//                }
//                is ControllableComponent -> {
//                    //skip
//                }
                else -> copyComponents.add(component)
            }
        }

        return copyComponents
    }
}