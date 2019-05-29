package com.gofficer.codenames.systems.server

import com.artemis.BaseSystem
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.gofficer.codenames.components.NetworkComponent
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.log.info
import java.util.concurrent.ConcurrentLinkedQueue
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.gofficer.codenames.*
import com.gofficer.codenames.systems.client.ClientNetworkSystem
import ktx.log.debug
import ktx.log.logger

/**
 * An Artemis system class, this is responsible for managing the KryoNet Server, generic outbound and all inbound packet
 * communication. It processes every loop.
 */
class ServerNetworkSystem(private val gameWorld: GameWorld, private val gameServer: GameServer) : BaseSystem() {


    companion object {
        val log = logger<ServerNetworkSystem>()
    }

    /**
     * keeps a tally of each packet type received and their frequency
     */
    private val debugPacketFrequencyByType = mutableMapOf<String, Int>()
    var packetsPerSecondTimer = GameTimer().apply { start() }
    var packetsReceivedPerSecond = 0
    var packetsReceivedPerSecondLast = 0

    val serverKryo: Server
    private val netQueue = ConcurrentLinkedQueue<NetworkJob>()

    inner class NetworkJob internal constructor(internal var connection: PlayerConnection,
                                                internal var receivedObject: Any)

    private fun processNetworkQueue() {
        while (netQueue.peek() != null) {
            val job: NetworkJob = netQueue.poll()

            NetworkHelper.debugPacketFrequencies(job.receivedObject, debugPacketFrequencyByType)


            receiveNetworkObject(job, job.receivedObject)

            packetsPerSecondTimer.resetIfExpired(1000) {
                packetsReceivedPerSecondLast = packetsReceivedPerSecond
                packetsReceivedPerSecond = 0
            }

            packetsReceivedPerSecond += 1

            if (GameSettings.debugPacketTypeStatistics) {
                log.debug { "--- packet type stats $debugPacketFrequencyByType" }
            }
        }

    }

    private fun receiveNetworkObject(job: NetworkJob, receivedObject: Any) {
        when (receivedObject) {
            is Network.Client.InitialClientData -> {
                log.debug { "Received initial client data "}
            }
//            is Network.Client.PlayerMove -> receivePlayerMove(job, receivedObject)
//            is Network.Client.ChatMessage -> receiveChatMessage(job, receivedObject)
//            is Network.Client.MoveInventoryItem -> receiveMoveInventoryItem(job, receivedObject)
//
//            is Network.Client.OpenDeviceControlPanel -> receiveOpenDeviceControlPanel(job, receivedObject)
//            is Network.Client.CloseDeviceControlPanel -> receiveCloseDeviceControlPanel(job, receivedObject)
//            is Network.Client.DoorOpen -> receiveDoorOpen(job, receivedObject)
//            is Network.Client.DeviceToggle -> receiveDeviceToggle(job, receivedObject)
//
//            is Network.Client.BlockDigBegin -> receiveBlockDigBegin(job, receivedObject)
//            is Network.Client.BlockDigFinish -> receiveBlockDigFinish(job, receivedObject)
//            is Network.Client.BlockPlace -> receiveBlockPlace(job, receivedObject)
//
//            is Network.Client.PlayerEquipHotbarIndex -> receivePlayerEquipHotbarIndex(job, receivedObject)
//            is Network.Client.InventoryDropItem -> receiveInventoryDropItem(job, receivedObject)
//            is Network.Client.PlayerEquippedItemAttack -> receivePlayerEquippedItemAttack(job, receivedObject)
//            is Network.Client.ItemPlace -> receiveItemPlace(job, receivedObject)


            is FrameworkMessage.Ping -> if (receivedObject.isReply) {

            }
            else -> if (receivedObject !is FrameworkMessage.KeepAlive) {
                assert(false) {
                    """Server network system, object was received but there's no
                        method calls to handle it, please add them.
                        Object: $receivedObject"""
                }
            }
        }
    }

    internal class PlayerConnection : Connection() {
        /**
         * entityid of the player
         */
        var playerEntityId: Int = 0
        var playerName: String = ""
    }

    //hack this needs fixed badly, none of this is thread safe for connect/disconnect
    internal inner class ServerListener : Listener() {
        //FIXME: do sanity checking (null etc) on both client, server
        override fun received(c: Connection?, obj: Any?) {
            val connection = c as PlayerConnection?
            netQueue.add(NetworkJob(connection!!, obj!!))

            //fixme, debug
            c!!.setTimeout(999999999)
            c.setKeepAliveTCP(9999999)
        }

        override fun connected(connection: Connection?) {
            super.connected(connection)

            //for more easily seeing which thread is which.
            Thread.currentThread().name = "server thread (main)"
        }

        override fun idle(connection: Connection?) {
            super.idle(connection)
        }

        override fun disconnected(c: Connection?) {
            val connection = c as PlayerConnection?
            connection?.let {
                log.debug { "Player disconnected"}
                // Announce to everyone that someone (with a registered playerName) has left.
                // TODO implement me
//                val chatMessage = Network.Server.ChatMessage(
//                    message = connection.playerName + " disconnected.",
//                    sender = Chat.ChatSender.Server
//                )

//                serverKryo.sendToAllTCP(chatMessage)
            }
        }
    }


    /**
     * Listener for notifying when a player has joined/disconnected,
     * systems and such interested can subscribe.
     */
    interface NetworkServerConnectionListener {
        /**
         * note this does not indicate when a connection *actually*
         * first happened, since we wouldn't have a player object,
         * and it wouldn't be valid yet.

         * @param playerEntityId
         */
        fun playerConnected(playerEntityId: Int) {

        }

        fun playerDisconnected(playerEntityId: Int) {
        }
    }

    init {
        serverKryo = object : Server(Network.bufferWriteSize, 2048) {
            override fun newConnection(): Connection {
                // By providing our own connection implementation, we can store per
                // connection state without a connection ID to state look up.
                return PlayerConnection()
            }
        }

        serverKryo.start()


        Network.register(serverKryo)


        serverKryo.addListener(ServerListener())


        serverKryo.bind(Network.PORT)

        //notify the local client we've started hosting our server, so he can connect now.
        gameWorld.server?.connectHostLatch?.countDown()
    }








    override fun processSystem() {
        processNetworkQueue()
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