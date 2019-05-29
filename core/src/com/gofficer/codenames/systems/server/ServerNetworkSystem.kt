package com.gofficer.codenames.systems.server

import com.artemis.BaseSystem
import com.artemis.Component
import com.artemis.utils.Bag
import com.badlogic.gdx.utils.Array
import java.util.concurrent.ConcurrentLinkedQueue
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.gofficer.codenames.*
import com.gofficer.codenames.components.CardComponent
import com.gofficer.codenames.components.PlayerComponent
import com.gofficer.codenames.components.TransformComponent
import com.gofficer.codenames.utils.mapper
import ktx.log.logger

/**
 * An Artemis system class, this is responsible for managing the KryoNet Server, generic outbound and all inbound packet
 * communication. It processes every loop.
 */
class ServerNetworkSystem(private val gameWorld: GameWorld, private val gameServer: GameServer) : BaseSystem() {


    companion object {
        val log = logger<ServerNetworkSystem>()
    }


    private val mPlayer by mapper<PlayerComponent>()
    private val mCard by mapper<CardComponent>()
    private val mTransform by mapper<TransformComponent>()


    /**
     * keeps a tally of each packet type received and their frequency
     */
    private val debugPacketFrequencyByType = mutableMapOf<String, Int>()
    var packetsPerSecondTimer = GameTimer().apply { start() }
    var packetsReceivedPerSecond = 0
    var packetsReceivedPerSecondLast = 0

    val serverKryo: Server

    val lastTick = 0

    private val netQueue = ConcurrentLinkedQueue<NetworkJob>()

    inner class NetworkJob internal constructor(internal var connection: PlayerConnection,
                                                internal var receivedObject: Any)


    private val connectionListeners = Array<NetworkServerConnectionListener>()

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
                log.debug { "Received initial client data ${receivedObject.playerUUID} "}
                receiveInitialClientData(job, receivedObject)
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
            log.debug{ "Adding item to net queue" }
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



    fun addConnectionListener(listener: NetworkServerConnectionListener) = connectionListeners.add(listener)

    private fun receiveInitialClientData(job: NetworkJob, initialClientData: Network.Client.InitialClientData) {
        var name = initialClientData.playerName

        if (name == null) {
            job.connection.close()
            return
        }
        //don't allow " " playername
        name = name.trim { it <= ' ' }

        if (name.isEmpty()) {
            //we don't bother sending a disconnection event. they'd know if something was a bad name or not (hacked client)
            job.connection.close()
            return
        }

        val uuid = initialClientData.playerUUID
        if (uuid == null) {
            job.connection.close()
            return
        }

        if (initialClientData.versionMajor != GameClient.VERSION_MAJOR ||
            initialClientData.versionMinor != GameClient.VERSION_MINOR ||
            initialClientData.versionRevision != GameClient.VERSION_MINOR) {
            val reason = Network.Shared.DisconnectReason()
            reason.reason = Network.Shared.DisconnectReason.Reason.VersionMismatch

            job.connection.sendTCP(reason)
            job.connection.close()
        }

        // Store the player on the connection.
        job.connection.playerEntityId = gameServer.createPlayer(name, job.connection.id)
        job.connection.playerName = name

        //notify to everyone it connected
        for (connectionListener in connectionListeners) {
            connectionListener.playerConnected(job.connection.playerEntityId)
        }
    }


    override fun processSystem() {

//        lastTick =
//        log.debug { "Tick" }
        processNetworkQueue()
    }

    /**
     * used for batch sending of heaps of entities to get spawned for the player/client

     * @param entitiesToSpawn
     * *
     * @param connectionPlayerId
     */
    fun sendSpawnMultipleEntities(entitiesToSpawn: List<Int>, connectionPlayerId: Int) {
        assert(entitiesToSpawn.isNotEmpty()) { "server told to spawn 0 entities, this is impossible" }

        val spawnMultiple = Network.Server.EntitySpawnMultiple()

        for (entityId in entitiesToSpawn) {
            if (mPlayer.has(entityId)) {
                //skip players we don't know how to spawn them automatically yet
                continue

                /*
                fixme hack to ignore all players. we dont' spawn them, but we're gonna
                need to rethink this. right now it is split between this generic spawning,
                and player spawning, which is a specific packet type sent out.

                we could make clients smart enough to know if that is their player..maybe
                also the bigger issue is we have no idea how to render them.

                i'm a bit confused in general as to how textures of entities will get rendered,
                or rather, which texture they know to use. once i make animations, this will
                make it that much harder. i'm not sure what a good model is to follow after,
                for animation states. especially when they relate to ECS.
                */
            }

//            val sprite = mSprite.get(entityId)
            val transform = mTransform.get(entityId).velocity
            val card = mCard.get(entityId)
            val spawn = Network.Server.EntitySpawn().apply {
                id = entityId

                components = serializeComponents(entityId)

                pos.set(transform.x, transform.y)
//                pos.set(sprite.sprite.x, sprite.sprite.y)
//                size.set(sprite.sprite.width, sprite.sprite.height)
                size.set(100f, 100f)
//                textureName = sprite.textureName!!
            }


            spawnMultiple.entitySpawn.add(spawn)
        }

        //logger.debug {"networkserversystem",
        //            "sending spawn multiple for ${spawnMultiple.entitySpawn!!.size} entities")
        log.debug { "Sending TCP to $connectionPlayerId" }
        serverKryo.sendToTCP(connectionPlayerId, spawnMultiple)
    }

    /**
     * Copies components into another array, skipping things that are not meant to be serialized
     * For instance, it does not serialize at all some bigger or useless things, like SpriteComponent,
     * PlayerComponent, things we never want to send from server->client

     * @param entityId
     * *
     * *
     * @return
     */
    private fun serializeComponents(entityId: Int): List<Component> {
        val components = Bag<Component>()

        world.getEntity(entityId).getComponents(components)

        val copyComponents = mutableListOf<Component>()
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

    fun sendDestroyMultipleEntities(entitiesToDestroy: List<Int>, connectionPlayerId: Int) {
        assert(entitiesToDestroy.isNotEmpty()) { "server told to destroy 0 entities, this is impossible" }

        val destroyMultiple = Network.Server.EntityDestroyMultiple()
        destroyMultiple.entitiesToDestroy = entitiesToDestroy

        //logger.debug {"networkserversystem",
        //            "sending destroy multiple for ${destroyMultiple.entitiesToDestroy!!.size} entities")
        serverKryo.sendToTCP(connectionPlayerId, destroyMultiple)
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