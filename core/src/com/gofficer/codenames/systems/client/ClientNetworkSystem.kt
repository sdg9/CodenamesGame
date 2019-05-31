package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.artemis.annotations.Wire
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.*
import com.gofficer.codenames.components.CardComponent
import com.gofficer.codenames.components.RevealedComponent
import com.gofficer.codenames.components.TextureComponent
import com.gofficer.codenames.components.TransformComponent
import com.gofficer.codenames.network.client.ClientResponseProcessor
import com.gofficer.codenames.network.client.GameNotificationProcessor
import com.gofficer.codenames.network.interfaces.INotification
import com.gofficer.codenames.network.interfaces.INotificationProcessor
import com.gofficer.codenames.network.interfaces.IResponse
import com.gofficer.codenames.network.interfaces.IResponseProcessor
import com.gofficer.codenames.utils.mapper
import ktx.log.debug
import ktx.log.logger
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetClientMarshalStrategy
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import net.mostlyoriginal.api.network.system.MarshalSystem


/**
 * Handles the network side of things, for the client
 */
@Wire
class ClientNetworkSystem(private val gameWorld: GameWorld) : MarshalSystem(Network.NetworkDictionary(), KryonetClientMarshalStrategy("127.0.0.1", Network.PORT)) {

    companion object {
        val log = logger<ClientNetworkSystem>()
    }


    var responseProcessor: IResponseProcessor = ClientResponseProcessor()
    var notificationProcessor: INotificationProcessor = GameNotificationProcessor()

    lateinit var clientKryo: Client
    private val networkStatusListeners = Array<NetworkClientListener>(5)

    private val netQueue = ConcurrentLinkedQueue<Any>()

    var connected: Boolean = false

    /**
     * the network id is a special id that is used to refer to an entity across
     * the network, for this client. Basically it is so the client knows what
     * entity id the server is talking about..as the client and server ECS engines
     * will have totally different sets of entitiy id's.
     *
     *
     * So, server sends over what its internal entity id is for an entity to spawn,
     * as well as for referring to future ones, and we make a map of ,
     * since we normally receive a server entity id, and we must determine what *our* (clients) entity
     * id is, so we can do things like move it around, perform actions etc on it.
     *
     *
     *
     *
     * server remote entity ID(key), client local entity id(value)
     */
    private val entityForNetworkId = HashMap<Int, Int>(500)

    /**
     * client local entity id(key), server remote entity ID(value)
     */
    private val networkIdForEntityId = HashMap<Int, Int>(500)

    private val mTexture by mapper<TextureComponent>()
    private val mCard by mapper<CardComponent>()
    private val mTransform by mapper<TransformComponent>()
    private val mRevealed by mapper<RevealedComponent>()

    /**
     * keeps a tally of each packet type received and their frequency
     */
    val debugPacketFrequencyByType = mutableMapOf<String, Int>()

    var packetsPerSecondTimer = GameTimer().apply { start() }
    var packetsReceivedPerSecond = 0
    var packetsReceivedPerSecondLast = 0

    fun addListener(listener: NetworkClientListener) {
        networkStatusListeners.add(listener)
    }


    interface NetworkClientListener {
        fun connected() {
        }

        //todo send a disconnection reason along with the disconnect event. to eg differentiate between a kick or a
        // connection loss, or a server shutdown
        fun disconnected(disconnectReason: Network.Shared.DisconnectReason) {
        }
    }


    internal inner class ClientListener : Listener() {

        override fun connected(connection: Connection?) {
            connection!!.setTimeout(999999999)
            log.debug { "our client connected!" }
        }

        //FIXME: do sanity checking (null etc) on both client, server
        override fun received(connection: Connection?, dataObject: Any?) {
//            log.debug { "ClientListener received object"}
            netQueue.add(dataObject)
        }

        override fun disconnected(connection: Connection?) {
        }
    }

    /**
     * connect the client network object to the given ip, at the given PORT

     * @param ip
     */
    @Throws(IOException::class)
    fun connect(ip: String, port: Int) {
        //clientKryo = new Client(16384, 8192, new JsonSerialization());
        clientKryo = Client(8192, Network.bufferObjectSize)
        clientKryo.start()

        Network.register(clientKryo)

        val lagMinMs = GameSettings.lagMinMs
        val lagMaxMs = GameSettings.lagMaxMs
        if (lagMinMs == 0 && lagMaxMs == 0) {
            //network latency debug switches unset, regular connection.
            log.debug { "Adding client listener" }
            clientKryo.addListener(ClientListener())
        } else {
            log.debug { "Adding client listener with lag details" }
            clientKryo.addListener(Listener.LagListener(lagMinMs, lagMaxMs, ClientListener()))
        }

        clientKryo.setKeepAliveTCP(999999)

        object : Thread("kryonet connection client thread") {
            override fun run() {
                try {
                    log.debug { "client attempting to connect to server" }
                    clientKryo.connect(99999999 /*fixme, debug*/, ip, port)
                    // Server communication after connection can go here, or in Listener#connected().

                    sendInitialClientData()
                    networkStatusListeners.forEach { it.connected() }
                } catch (ex: IOException) {
                    //fixme this is horrible..but i can't figure out how to rethrow it back to the calling thread
                    //throw new IOException("tesssst");
                    //                    ex.printStackTrace();
                    System.exit(1)
                }

            }
        }.start()

    }

    private fun sendInitialClientData() {
        val initialClientData = Network.Client.InitialClientData().apply {
            playerName = GameSettings.playerName
            //TODO generate some random thing
            playerUUID = UUID.randomUUID().toString()
            versionMajor = GameClient.VERSION_MAJOR
            versionMinor = GameClient.VERSION_MINOR
            versionRevision = GameClient.VERSION_REVISION
        }

        clientKryo.sendTCP(initialClientData)
    }


    var pingTimer = GameTimer()


    override fun received(connectionId: Int, obj: Any?) {
        Gdx.app.postRunnable {
            Log.info(obj.toString())
            when (obj) {
                is IResponse -> obj.accept(responseProcessor)
                is INotification -> obj.accept(notificationProcessor)
            }
        }
//        super.received(connectionId, `object`)
    }


    override fun processSystem() {

//        log.debug { "Process System Tick" }
        processNetworkQueue()

        if (pingTimer.resetIfExpired(1000)) {
            clientKryo.updateReturnTripTime()
            val time = clientKryo.returnTripTime
        }
    }

    private fun processNetworkQueue() {
        while (netQueue.peek() != null) {
            val receivedObject = netQueue.poll()
            receiveNetworkObject(receivedObject)

            packetsPerSecondTimer.resetIfExpired(1000) {
                packetsReceivedPerSecondLast = packetsReceivedPerSecond
                packetsReceivedPerSecond = 0
            }

            packetsReceivedPerSecond += 1

            NetworkHelper.debugPacketFrequencies(receivedObject, debugPacketFrequencyByType)
        }

        if (GameSettings.debugPacketTypeStatistics) {
            log.debug { "--- packet type stats $debugPacketFrequencyByType" }
        }
    }

    private fun receiveNetworkObject(receivedObject: Any) {
//        log.debug { "Received network object" }
        when (receivedObject) {
            is Network.Shared.DisconnectReason -> debug { "Disconnect ${receivedObject.reason}"}
//            is Network.Server.PlayerSpawned -> receivePlayerSpawn(receivedObject)
            // TODO insert more types

//            is Network.Shared.BlockRegion -> receiveBlockRegion(receivedObject)
//            is Network.Shared.SparseBlockUpdate -> receiveSparseBlockUpdate(receivedObject)
//
//            is Network.Server.LoadedViewportMoved -> receiveLoadedViewportMoved(receivedObject)
//            is Network.Server.SpawnInventoryItems ->
//                receivePlayerSpawnInventoryItems(receivedObject)
//
//            is Network.Server.PlayerSpawned -> receivePlayerSpawn(receivedObject)
//            //} else if (receivedObject instanceof Network.EntitySpawnFromServer) {
//
            is Network.Server.EntitySpawnMultiple -> receiveEntitySpawnMultiple(receivedObject)
//            is Network.Server.EntityDestroyMultiple -> receiveMultipleEntityDestroy(receivedObject)
//            is Network.Server.EntityKilled -> receiveEntityKilled(receivedObject)
//            is Network.Server.EntityMoved -> receiveEntityMoved(receivedObject)
//            is Network.Server.EntityHealthChanged -> receiveEntityHealthChanged(receivedObject)
//
//            is Network.Server.UpdateGeneratorControlPanelStats -> receiveUpdateGeneratorControlPanelStats(
//                receivedObject)
//
//            is Network.Server.ChatMessage -> receiveChatMessage(receivedObject)
//            is Network.Server.PlayerAirChanged -> receiveAirChanged(receivedObject)
            is Network.Server.CardTouched -> receiveCardTouched(receivedObject)
//            is Network.Server.DoorOpen -> receiveDoorOpen(receivedObject)

            is FrameworkMessage.Ping -> {
            }

            else -> if (receivedObject !is FrameworkMessage.KeepAlive) {
                assert(false) {
                    """Client network system, object was received but there's no
                        method calls to handle it, please add them.
                        Object: ${receivedObject.toString()}"""
                }
            } else {
                log.debug { "Unmatched object $receivedObject"}
            }
        }
    }

    private fun receiveCardTouched(activated: Network.Server.CardTouched) {
        val localId = entityForNetworkId[activated.entityId]!!
        mCard.get(localId)
        mRevealed.set(localId, true)
    }

    private fun receiveDisconnectReason(disconnectReason: Network.Shared.DisconnectReason) {
        networkStatusListeners.forEach { listener -> listener.disconnected(disconnectReason) }
    }

    private fun receiveEntitySpawnMultiple(entitySpawn: Network.Server.EntitySpawnMultiple) {
        // TODO continue here
        log.debug { "I should spawn $entitySpawn" }
//        //fixme this and hotbar code needs consolidation
//        //logger.debug {"client receiveMultipleEntitySpawn", "entities: " + spawnFromServer.entitySpawn);
//
//        //var debug = "receiveMultipleEntitySpawn [ "
        for (spawn in entitySpawn.entitySpawn) {

            val localEntityId = getWorld().create()

            log.debug { "Spawn: ${spawn.id} at ${spawn.pos} for local entity id $localEntityId"}
            // debug += " networkid: " + spawn.id + " localid: " + e

            for (c in spawn.components) {
                val entityEdit = getWorld().edit(localEntityId)
                entityEdit.add(c)
            }

            // TODO alter to figure out how to pass texture, or id/value indicating texture, via server
            mTexture.create(localEntityId).apply {
                texture = gameWorld?.client?.cardTexture
            }

            // TODO Transform and card (and future) can be handled with entity Edit
//            mTransform.create(localEntityId).apply {
//                velocity = Vector2(0f, 0f)
//            }

//            mCard.create(localEntityId).apply {
//                cardName = "test"
//                cardColor = Color.BLUE
//            }
//            //fixme id..see above.
//            val cSprite = mSprite.create(localEntityId).apply {
//                textureName = spawn.textureName
//                sprite.setSize(spawn.size.x, spawn.size.y)
//                sprite.setPosition(spawn.pos.x, spawn.pos.y)
//            }
//
//            val cGenerator = mGenerator.opt(localEntityId)?.let {
//                //recreate this on our end. since it is transient
//                it.fuelSources = GeneratorInventory(GeneratorInventory.MAX_SLOTS, world)
//            }
//
//            require(cSprite.textureName != null)
//
//            val textureRegion: TextureRegion?
//            if (mBlock.has(localEntityId)) {
//                textureRegion = tileRenderSystem.blockAtlas.findRegion(cSprite.textureName)
//            } else {
//                textureRegion = gameWorld.atlas.findRegion(cSprite.textureName)
//            }
//
//            require(textureRegion != null) {
//                "texture region is null on receiving entity spawn and reverse lookup of texture for" +
//                        " this entity, texturename: ${cSprite.textureName} category: ${cSprite.category}"
//            }
//
//            cSprite.sprite.setRegion(textureRegion)
//
//            //keep our networkid -> localid mappings up to date
//            //since the client and server can never agree on which id to make an
//            //entity as, so we must handshake after the fact
            val result1 = networkIdForEntityId.put(localEntityId, spawn.id)
            val result2 = entityForNetworkId.put(spawn.id, localEntityId)

            if (result1 != null) {
                assert(false) {
                    """put failed for spawning, into entity bidirectional map, value already existed id: $localEntityId
                    networkid: ${spawn.id}"""
                }
            }

            require(result2 == null) { "put failed for spawning, into entity bidirectional map, value already existed" }

            require(entityForNetworkId.size == networkIdForEntityId.size) {
                "spawn, network id and entity id maps are out of sync(size mismatch)"
            }
        }

        //logger.debug {"networkclientsystem", debug)
    }

    fun sendCardTouched(entityId: Int) {
        val networkId = networkIdForEntityId[entityId]!!
        val cardPress = Network.Client.EntityTouch(networkId)


        clientKryo.sendTCP(cardPress)
    }

//    private fun receivePlayerSpawn(spawn: Network.Server.PlayerSpawned) {
//        //it is our main player (the client's player, aka us)
//        if (!connected) {
//            //fixme not ideal, calling into the client to do this????
////            val player = gameWorld.client!!.createPlayer(spawn.playerName, clientKryo.id, true)
////            val spriteComp = mSprite.get(player)
////
////            spriteComp.sprite.setPosition(spawn.pos.x, spawn.pos.y)
////
////            val playerSprite = mSprite.get(player)
////            playerSprite.sprite.setRegion(gameWorld.atlas.findRegion("player-32x64"))
////
////            val aspectSubscriptionManager = getWorld().aspectSubscriptionManager
////            val subscription = aspectSubscriptionManager.get(allOf())
////            subscription.addSubscriptionListener(ClientEntitySubscriptionListener())
////
////            val cAir = mAir.get(player)
////            gameWorld.client!!.hud.airChanged(cAir, cAir.air)
//
//            connected = true
//
//            //notify we connected
//            networkStatusListeners.forEach { it.connected() }
//        } else {
//            //FIXME cover other players joining case
//            //       throw RuntimeException("fixme, other players joining not yet implemented")
//        }
//    }
}