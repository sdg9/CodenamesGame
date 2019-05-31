package com.gofficer.codenames.systems.server

import com.gofficer.codenames.GameServer
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.Network
import com.gofficer.codenames.NetworkDictionary
import com.gofficer.codenames.network.interfaces.INotification
import com.gofficer.codenames.network.interfaces.INotificationProcessor
import com.gofficer.codenames.network.interfaces.IRequest
import com.gofficer.codenames.network.interfaces.IRequestProcessor
import com.gofficer.codenames.network.server.ServerNotificationProcessor
import com.gofficer.codenames.network.server.ServerRequestProcessor
import ktx.log.logger
import net.mostlyoriginal.api.network.marshal.common.MarshalStrategy
import net.mostlyoriginal.api.network.system.MarshalSystem
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque

class ServerNetworkSystem @JvmOverloads constructor(


    private val gameWorld: GameWorld,
    private val gameServer: GameServer,
    private val strategy: MarshalStrategy,
    private val requestProcessor: IRequestProcessor = ServerRequestProcessor(gameWorld),
    private val notificationProcessor: INotificationProcessor = ServerNotificationProcessor(gameWorld)
) :
    MarshalSystem(NetworkDictionary(), strategy) {
    private val netQueue = ConcurrentLinkedDeque<NetworkJob>()

    companion object {
        val log = logger<ServerNetworkSystem>()
    }

    init {
        gameServer?.connectHostLatch?.countDown()
        start()
    }

    override fun received(connectionId: Int, obj: Any) {
        netQueue.add(NetworkJob(connectionId, obj))
    }

    private fun processJob(job: NetworkJob) {
        val connectionId = job.connectionId
        val obj = job.receivedObject
        when (obj) {
            is IRequest -> obj.accept(requestProcessor, connectionId)
            is INotification -> obj.accept(notificationProcessor)
        }
    }

    override fun processSystem() {
        super.processSystem()
        while (netQueue.peek() != null) {
            processJob(netQueue.poll())
        }
    }

    override fun connected(connectionId: Int) {
        super.connected(connectionId)
        log.debug { "Player $connectionId connected"}
    }

    override fun disconnected(connectionId: Int) {
        super.disconnected(connectionId)
//        getServer().ifPresent { server ->
//            if (!server.networkManager.connectionHasPlayer(connectionId)) {
//            } else {
//                val playerToDisconnect = server.networkManager.getPlayerByConnection(connectionId)
//                if (playerToDisconnect != null) {
////                server.getMapManager().removeEntity(playerToDisconnect)
//                    server.networkManager.unregisterUserConnection(playerToDisconnect, connectionId)
////                server.getWorldManager().unregisterEntity(playerToDisconnect)
//                }
//            }
//
//        }
    }
//
//    fun getServer(): Optional<Server> {
//        return Optional.ofNullable<Server>(gameServer)
//    }

    class NetworkJob internal constructor(val connectionId: Int, val receivedObject: Any)
}
