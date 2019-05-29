package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.badlogic.gdx.utils.Array
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.gofficer.codenames.*
import ktx.log.debug
import ktx.log.logger
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * Handles the network side of things, for the client
 */
class ClientNetworkSystem(private val oreWorld: GameWorld) : BaseSystem() {

    companion object {
        val log = logger<ClientNetworkSystem>()
    }

    lateinit var clientKryo: Client
    private val networkStatusListeners = Array<NetworkClientListener>(5)

    private val netQueue = ConcurrentLinkedQueue<Any>()

    var connected: Boolean = false
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
            clientKryo.addListener(ClientListener())
        } else {
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

    override fun processSystem() {
        processNetworkQueue()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        when (receivedObject) {
            is Network.Shared.DisconnectReason -> debug { "Disconnect ${receivedObject.reason}"}
            is Network.Server.PlayerSpawned -> receivePlayerSpawn(receivedObject)
            // TODO insert more types

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

    private fun receiveDisconnectReason(disconnectReason: Network.Shared.DisconnectReason) {
        networkStatusListeners.forEach { listener -> listener.disconnected(disconnectReason) }
    }

    private fun receivePlayerSpawn(spawn: Network.Server.PlayerSpawned) {
        //it is our main player (the client's player, aka us)
        if (!connected) {
            //fixme not ideal, calling into the client to do this????
//            val player = oreWorld.client!!.createPlayer(spawn.playerName, clientKryo.id, true)
//            val spriteComp = mSprite.get(player)
//
//            spriteComp.sprite.setPosition(spawn.pos.x, spawn.pos.y)
//
//            val playerSprite = mSprite.get(player)
//            playerSprite.sprite.setRegion(oreWorld.atlas.findRegion("player-32x64"))
//
//            val aspectSubscriptionManager = getWorld().aspectSubscriptionManager
//            val subscription = aspectSubscriptionManager.get(allOf())
//            subscription.addSubscriptionListener(ClientEntitySubscriptionListener())
//
//            val cAir = mAir.get(player)
//            oreWorld.client!!.hud.airChanged(cAir, cAir.air)

            connected = true

            //notify we connected
            networkStatusListeners.forEach { it.connected() }
        } else {
            //FIXME cover other players joining case
            //       throw RuntimeException("fixme, other players joining not yet implemented")
        }
    }
}