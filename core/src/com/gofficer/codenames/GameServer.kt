package com.gofficer.codenames

import ktx.log.logger
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetServerMarshalStrategy
import java.util.concurrent.CountDownLatch

/**
The Runnable class that orchestrates server state. It’s run in a separate thread on the host’s machine. Whether someone
chooses to play singleplayer or multiplayer, the game treats it all the same and will run the GameServer thread. It was
a lot easier to develop the game this way than to have conditionals everywhere with strategy-this, factory-that. It runs
its own Artemis World, separate from the client.
 */
class GameServer() : Runnable {

    companion object {
        val log = logger<GameServer>()
    }

    var connectHostLatch = CountDownLatch(1)
    var shutdownLatch = CountDownLatch(1)


    private val SERVER_FIXED_TIMESTEP = 1.0 / 60.0 * 1000

    private var hostingPlayer: Int? = null

    lateinit var gameWorld: GameWorld

//    private var strategy: KryonetServerMarshalStrategy? = null
//    private var players: Set<Network.Shared.Player>? = null

    override fun run() {
        Thread.currentThread().name = "server thread (main)"

        log.debug { "Creating server game world"}
        gameWorld = GameWorld(client = null, server = this, worldInstanceType = GameWorld.WorldInstanceType.Server)

        gameWorld.init()

        gameWorld.artemisWorld.inject(this, true)
        //exit the server thread when the client notifies us to,
        //by setting the latch to 0,
        //the client notifies us to exit it ASAP
        while (gameWorld.server!!.shutdownLatch.count != 0L) {
//            log.debug { "tick" }
            gameWorld.process()
        }

        // TODO: Save game state on shutdown?
        //shutdown saving
//        if (OreSettings.saveLoadWorld) {
//            gameWorld.worldIO.saveWorld()
//        }

        gameWorld.shutdown()
    }

    /**
     * @param playerName
     * *
     * @param connectionId
     * *
     * *
     * @return entity id
     */
    fun createPlayer(playerName: String, connectionId: Int): Int {
        val player = gameWorld.entityFactory.createPlayer(playerName, connectionId)
        return player
    }

}
