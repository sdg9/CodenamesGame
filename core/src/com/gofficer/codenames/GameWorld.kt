package com.gofficer.codenames


import com.artemis.managers.TagManager
import ktx.log.debug
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.PlayerManager
import com.gofficer.codenames.network.server.ServerNotificationProcessor
import com.gofficer.codenames.network.server.ServerRequestProcessor
import com.gofficer.codenames.systems.*
import com.gofficer.codenames.systems.client.*
import com.gofficer.codenames.systems.server.*
import com.gofficer.codenames.utils.gameInject
import com.gofficer.codenames.systems.server.GameLoopSystemInvocationStrategy
import ktx.log.logger
import net.mostlyoriginal.api.event.common.EventSystem
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetServerMarshalStrategy


/**
 * The main world, shared between both client and server, core to a lot of basic
 * shared functionality, as well as stuff that doesn't really belong elsewhere,
 * creates the artemis world and handles processing, as well as shutting down
 * (when told to do so)

 * @param client
 *         never null..
 *
 * @param server
 *          null if it is only a client, if both client and server are valid, the
 *          this is a local hosted server, (aka singleplayer, or self-hosting)
 */
class GameWorld
    (var client: GameClient?,
     var server: GameServer?,
     var worldInstanceType: GameWorld.WorldInstanceType) {

    companion object {
        val log = logger<GameWorld>()
    }

    lateinit var artemisWorld: World
    lateinit var entityFactory: EntityFactory

    var worldGenerator: WorldGenerator? = null

    fun init() {
        if (worldInstanceType == WorldInstanceType.Client || worldInstanceType == WorldInstanceType.ClientHostingServer) {
            initClient()
        } else if (isServer) {
            initServer()
        }
    }

    /**
     * who owns/is running this exact world instance. If it is the server, or a client.
     * Note that if the connection type is only a client, obviously a server
     * world type will never exist
     */
    enum class WorldInstanceType {
        //strictly a client. join only
        Client,
        //dedicated server
        Server,
        //it's a client that also happens to be hosting a game (server)
        ClientHostingServer
    }

    val isServer = worldInstanceType == WorldInstanceType.Server
    val isClient = worldInstanceType == WorldInstanceType.Client ||
            worldInstanceType == WorldInstanceType.ClientHostingServer


    fun initClient() {
        log.debug { "Init client" }

        artemisWorld = World(WorldConfigurationBuilder()
            .with(
                ClientNetworkSystem(this, "127.0.0.1", Network.PORT),
                ClientNetworkEntitySystem(),
                EventSystem(),
                TagManager(),
                TextureManager(client!!.assetManager),
                CameraSystem(),
                TouchSystem(),
                SharedWorldManager(this),
                CardPressedSystem(),
                ClearScreenSystem(),
                TextureResolverSystem(this),
//                FlipAnimationSystem(),
                CardRenderSystem(gameWorld = this),
                RemoveSystem()
            ).build())

        //inject the mappers into the world, before we start doing things
        artemisWorld.inject(this, true)

        entityFactory = EntityFactory(this)

    }

    fun initServer() {

        log.debug { "Init server" }
        val strategy = KryonetServerMarshalStrategy("127.0.0.1", Network.PORT)
        artemisWorld = World(WorldConfigurationBuilder()
            .with(
                TagManager(),
//                SpatialSystem(this),
                PlayerManager(),
                ServerNetworkSystem(this, server!!, strategy, ServerRequestProcessor(this), ServerNotificationProcessor(this)),
                ServerNetworkEntitySystem(),
                NetworkManager(server!!, strategy),
                SharedWorldManager(this),
                WorldManager(this, server!!)
            )
            .register(GameLoopSystemInvocationStrategy(msPerLogicTick = 25, isServer = true))
            .build())
        //inject the mappers into the world, before we start doing things
        artemisWorld.gameInject(this)

        entityFactory = EntityFactory(this)

        worldGenerator = WorldGenerator(this)

        worldGenerator!!.generateGame()
    }

    /**
     * main world processing,
     * will handle all logic/render processing,
     * as it delegates this to the ECS, which handles
     * ordering and so on.
     */
    fun process() {
        artemisWorld.process()
    }

    fun shutdown() {
        debug { "Shutting down..." }
        artemisWorld?.dispose()
    }


}



