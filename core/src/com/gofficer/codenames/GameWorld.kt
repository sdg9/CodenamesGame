package com.gofficer.codenames


import com.artemis.ComponentMapper
import com.artemis.managers.TagManager
import com.gofficer.codenames.components.CardComponent
import ktx.log.debug
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.PlayerManager
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.systems.GameLoopSystemInvocationStrategy
import com.gofficer.codenames.systems.SpatialSystem
import com.gofficer.codenames.systems.TestSystem
import com.gofficer.codenames.systems.client.ClientNetworkSystem
import com.gofficer.codenames.systems.client.CardRenderSystem
import com.gofficer.codenames.systems.client.TextureResolverSystem
import com.gofficer.codenames.systems.client.TouchSystem
import com.gofficer.codenames.systems.server.ServerNetworkEntitySystem
import com.gofficer.codenames.systems.server.ServerNetworkSystem
import com.gofficer.codenames.utils.gameInject
import ktx.log.logger


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

    var worldGenerator: WorldGenerator? = null


    private lateinit var tagManager: TagManager

    private lateinit var mCard: ComponentMapper<CardComponent>

    lateinit var entityFactory: EntityFactory

    lateinit var camera: OrthographicCamera
    lateinit var viewport: FitViewport
    lateinit var uiCamera: OrthographicCamera
    lateinit var uiViewport: FitViewport

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
        initCamera()

        log.debug { "Init client" }
//        atlas = TextureAtlas(file("packed/entities.atlas"))

        //note although it may look like it.. order between render and logic ones..actually doesn't matter, their base
        // class dictates this. order between ones of the same type, does though.
        artemisWorld = World(WorldConfigurationBuilder()
            .with(
                TagManager(),
                TestSystem(),
                TextureResolverSystem(this),
                TouchSystem(this, uiCamera),
                ClientNetworkSystem(this),
                CardRenderSystem(gameWorld = this, camera = camera)
            ).build())

//            WorldConfigurationBuilder().register(
//                GameLoopSystemInvocationStrategy(msPerLogicTick = 25,
//            isServer = false)
//            )
//            .with(TagManager())
//            .with(PlayerManager())
////            .with(MovementSystem(this))
////            .with(SoundSystem(this))
//            .with(TestSystem())
//            .with(ClientNetworkSystem(this))
////            .with(InputSystem(camera, this))
////            .with(EntityOverlaySystem(this))
////            .with(PlayerSystem(this))
////            .with(GameTickSystem(this))
////            .with(ClientBlockDiggingSystem(this, client!!))
////            .with(BackgroundRenderSystem(oreWorld = this, camera = client!!.viewport.camera))
////            .with(TileRenderSystem(camera = camera,
////                fullscreenCamera = client!!.viewport.camera,
////                oreWorld = this))
////            .with(SpriteRenderSystem(camera = camera,
////                oreWorld = this))
////            .with(LiquidRenderSystem(camera = camera, oreWorld = this))
////            .with(DebugTextRenderSystem(camera, this))
////            .with(PowerOverlayRenderSystem(oreWorld = this,
////                fullscreenCamera = client!!.viewport.camera,
////                stage = client!!.stage))
////            .with(TileTransitionSystem(camera, this))
////            .register(GameLoopSystemInvocationStrategy(msPerLogicTick = 25, isServer = false))
//            .build())
        //b.dependsOn(WorldConfigurationBuilder.Priority.LOWEST + 1000,ProfilerSystem.class);

        //inject the mappers into the world, before we start doing things
        artemisWorld.inject(this, true)

        entityFactory = EntityFactory(this)
    }

    private fun initCamera() {
        camera = OrthographicCamera()
        viewport = FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, camera)
        uiCamera = OrthographicCamera()
        uiViewport = FitViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT, uiCamera)

    }

    fun initServer() {

        log.debug { "Init server" }
        artemisWorld = World(WorldConfigurationBuilder()
            .with(TagManager())
            .with(SpatialSystem(this))
            .with(PlayerManager())
//            .with(AISystem(this))
//            .with(MovementSystem(this))
//            .with(ServerPowerSystem(this))
//            .with(GameTickSystem(this))
//            .with(DroppedItemPickupSystem(this))
//            .with(GrassBlockSystem(this))
            .with(ServerNetworkEntitySystem(server!!))
//            .with(ServerBlockDiggingSystem(this))
//            .with(PlayerSystem(this))
//            .with(ExplosiveSystem(this))
//            .with(AirSystem(this))
            .with(ServerNetworkSystem(this, server!!))
//            .with(TileLightingSystem(this))
//            .with(LiquidSimulationSystem(this))
            .register(GameLoopSystemInvocationStrategy(msPerLogicTick = 25, isServer = true))
            .build())
        //inject the mappers into the world, before we start doing things
        artemisWorld.gameInject(this)

        // TODO initial card generations

        entityFactory = EntityFactory(this)

        worldGenerator = WorldGenerator(this)

        worldGenerator!!.generateGame()
        // TODO make starting works
        //severe: obviously...we don't want to do this right after..we can't save the world while we're still generating it
//        if (OreSettings.saveLoadWorld) {
//            worldIO.saveWorld()
//        }
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



