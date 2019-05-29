package com.gofficer.codenames

import java.util.concurrent.CountDownLatch

/**
The Runnable class that orchestrates server state. It’s run in a separate thread on the host’s machine. Whether someone
chooses to play singleplayer or multiplayer, the game treats it all the same and will run the GameServer thread. It was
a lot easier to develop the game this way than to have conditionals everywhere with strategy-this, factory-that. It runs
its own Artemis World, separate from the client.
 */
class GameServer() : Runnable {
    var connectHostLatch = CountDownLatch(1)
    var shutdownLatch = CountDownLatch(1)


    private val SERVER_FIXED_TIMESTEP = 1.0 / 60.0 * 1000

    private var hostingPlayer: Int? = null

    lateinit var gameWorld: GameWorld

    override fun run() {
        Thread.currentThread().name = "server thread (main)"

        gameWorld = GameWorld(client = null, server = this, worldInstanceType = GameWorld.WorldInstanceType.Server)

        gameWorld.init()

        gameWorld.artemisWorld.inject(this, true)


        // TODO Chat

        //exit the server thread when the client notifies us to,
        //by setting the latch to 0,
        //the client notifies us to exit it ASAP
        while (gameWorld.server!!.shutdownLatch.count != 0L) {
            gameWorld.process()
        }


        //shutdown saving
//        if (OreSettings.saveLoadWorld) {
//            gameWorld.worldIO.saveWorld()
//        }

        gameWorld.shutdown()
    }

}
