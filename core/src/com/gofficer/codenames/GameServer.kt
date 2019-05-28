package com.gofficer.codenames

/**
The Runnable class that orchestrates server state. It’s run in a separate thread on the host’s machine. Whether someone
chooses to play singleplayer or multiplayer, the game treats it all the same and will run the GameServer thread. It was
a lot easier to develop the game this way than to have conditionals everywhere with strategy-this, factory-that. It runs
its own Artemis World, separate from the client.
 */
class GameServer() : Runnable {

    lateinit var gameWorld: GameWorld

    override fun run() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        Thread.currentThread().name = "server thread (main)"

        gameWorld = GameWorld(client = null, server = null, worldInstanceType = GameWorld.WorldInstanceType.Server)

        gameWorld.init()


        //exit the server thread when the client notifies us to,
        //by setting the latch to 0,
        //the client notifies us to exit it ASAP
        while (gameWorld.server!!.shutdownLatch.count != 0L) {
            gameWorld.process()
        }
    }

}