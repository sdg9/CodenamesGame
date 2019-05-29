package com.gofficer.codenames

import ktx.log.logger
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
//            log.debug { "tick" }
            gameWorld.process()
        }


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

//        //the first player in the world, if server is hosted by the client (same machine & process)
//        if (hostingPlayer == null) {
//            hostingPlayer = player
//        }
//
//        //TODO:make better server player first-spawning code(in new world), find a nice spot to spawn in
//        //and then TODO: (much later) make it try to load the player position from previous world data, if any.
//        val posX = worldSize.width * 0.5f
//        var posY = oreWorld.findSolidGround(posX) - 2f
//        val tilex = posX.toInt()
//        val tiley = posY.toInt()
//
//        val seaLevel = oreWorld.seaLevel()
//
//        //collision test
//        //left
//        for (y in 0 until seaLevel) {
//            //oreWorld.blockAt(tilex - 24, tiley + y).type = Block.BlockType.Stone;
//        }
//
//        //right
//        for (y in 0 until seaLevel) {
//            //        oreWorld->blockAt(tilex + 24, tiley + y).primitiveType = Block::Stone;
//        }
//        //top
//        for (x in tilex - 54 until tilex + 50) {
//            //oreWorld.blockAt(x, tiley).type = Block.BlockType.Stone;
//        }
//
//        val playerSprite = mSprite.get(player).apply {
//            sprite.setPosition(posX, posY)
//        }
//
//        val cPlayer = mPlayer.get(player).apply {
//            hotbarInventory = HotbarInventory(Inventory.maxHotbarSlots, oreWorld.artemisWorld)
//            hotbarInventory!!.addListener(HotbarInventorySlotListener())
//
//            oreWorld.artemisWorld.inject(hotbarInventory!!)
//        }
//
//        cPlayer.inventory = Inventory(Inventory.maxSlots, oreWorld.artemisWorld)
//        oreWorld.artemisWorld.inject(cPlayer.inventory!!)
//
//        //FIXME UNUSED, we use connectionid instead anyways        ++freePlayerId;
//
//        //tell all players including himself, that he joined
//        serverNetworkSystem.sendSpawnPlayerBroadcast(player)
//
//        //give this player the list of other players who are connected
//        oreWorld.players().filter { playerEntity -> playerEntity != player }.forEach { playerEntity ->
//            //exclude himself, though. he already knows.
//            serverNetworkSystem.sendSpawnPlayer(playerEntity, connectionId)
//        }
//
//        //load players main inventory and hotbar, but be sure to do it after he's been told
//        //to have spawned in the world
//        loadInventory(player)
//        loadHotbarInventory(player)
//
//        sendServerMessage("Player ${cPlayer.playerName} has joined the server")
//
//        val bunny = oreWorld.entityFactory.createBunny()
//
//        val bunnyX = playerSprite.sprite.x
//        val bunnyY = oreWorld.findSolidGround(bunnyX)
//        val cBunnySprite = mSprite.get(bunny).apply {
//            //this.sprite.setPosition()
//            this.sprite.setPosition(bunnyX, bunnyY -1f)
//        }

        return player
    }

}
