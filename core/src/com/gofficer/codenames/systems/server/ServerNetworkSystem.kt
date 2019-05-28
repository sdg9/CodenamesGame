package com.gofficer.codenames.systems.server

import com.artemis.BaseSystem
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.gofficer.codenames.GameServer
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.NetworkComponent
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.log.info
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * An Artemis system class, this is responsible for managing the KryoNet Server, generic outbound and all inbound packet
 * communication. It processes every loop.
 */
class ServerNetworkSystem(private val gameWorld: GameWorld, private val gameServer: GameServer) : BaseSystem() {

    inner class NetworkJob internal constructor(internal var connection: PlayerConnection,
                                                internal var receivedObject: Any)

    private val netQueue = ConcurrentLinkedQueue<NetworkJob>()


    override fun processSystem() {
        processNetworkQueue()
    }

    private fun processNetworkQueue() {
        while (netQueue.peek() != null) {
            val job: NetworkJob = netQueue.poll()

            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        }

    }

}
//class ServerNetworkSystem : IteratingSystem(allOf(NetworkComponent::class).get()) {
//
//    override fun processEntity(entity: Entity?, deltaTime: Float) {
////        serverNetworkSystem.sentEntity(entity)
////
////        info { "Send $entity over the wire"}
////        entity?.remove<NetworkComponent>()
//    }
//
//}