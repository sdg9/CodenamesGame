package com.gofficer.codenames.systems.server

import com.gofficer.codenames.GameServer
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetServerMarshalStrategy
import java.util.concurrent.ConcurrentHashMap


class NetworkManager(server: GameServer, private val strategy: KryonetServerMarshalStrategy) : DefaultManager(server) {

    private val playerByConnection = ConcurrentHashMap<Int, Int>()
    private val connectionByPlayer = ConcurrentHashMap<Int, Int>()

    fun stop() {
        strategy.stop()
    }

    /**
     * Object will be serialized and sent using kryo
     *
     * @param id     connection ID
     * @param packet Object to send
     */
    fun sendTo(id: Int, packet: Any) {
        strategy.sendTo(id, packet)
    }

    fun registerUserConnection(playerId: Int, connectionId: Int) {
        playerByConnection[connectionId] = playerId
        connectionByPlayer[playerId] = connectionId
    }

    fun unregisterUserConnection(playerId: Int, connectionId: Int) {
        playerByConnection.remove(connectionId, playerId)
        connectionByPlayer[playerId] = connectionId
    }

    fun connectionHasPlayer(connectionId: Int): Boolean {
        return playerByConnection.containsKey(connectionId)
    }

    fun playerHasConnection(player: Int): Boolean {
        return connectionByPlayer.containsKey(player)
    }

    fun getPlayerByConnection(connectionId: Int): Int? {
        return playerByConnection[connectionId]
    }

    fun getConnectionByPlayer(playerId: Int): Int? {
        return connectionByPlayer[playerId]
    }

}
