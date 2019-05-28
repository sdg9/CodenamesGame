package com.gofficer.codenames


import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.PlayerManager
import com.artemis.managers.TagManager
import com.artemis.systems.EntityProcessingSystem
import com.artemis.utils.IntBag
import com.artemis.utils.reflect.ClassReflection
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.RandomXS128
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import ktx.assets.file

@Suppress("NOTHING_TO_INLINE")

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
    (var client: OreClient?,
     var server: OreServer?,
     var worldInstanceType: OreWorld.WorldInstanceType,
     val worldSize: WorldSize) {

    companion object : KLogging() {
        const val BLOCK_SIZE_PIXELS = 16.0f

        /**
         * @see WorldGenerator
         */
        val WORLD_SEA_LEVEL = 50

        const val s_itemPlacementOverlay = "itemPlacementOverlay"
        const val s_crosshair = "crosshair"
        const val s_mainPlayer = "mainPlayer"
    }

    private lateinit var tagManager: TagManager

    private lateinit var mPlayer: ComponentMapper<PlayerComponent>
    private lateinit var mDoor: ComponentMapper<DoorComponent>
    private lateinit var mSprite: ComponentMapper<SpriteComponent>
    private lateinit var mControl: ComponentMapper<ControllableComponent>
    private lateinit var mItem: ComponentMapper<ItemComponent>
    private lateinit var mVelocity: ComponentMapper<VelocityComponent>
    private lateinit var mJump: ComponentMapper<JumpComponent>
    private lateinit var mBlock: ComponentMapper<BlockComponent>
    private lateinit var mTool: ComponentMapper<ToolComponent>
    private lateinit var mAir: ComponentMapper<AirComponent>
    private lateinit var mHealth: ComponentMapper<HealthComponent>
    private lateinit var mLight: ComponentMapper<LightComponent>
    private lateinit var mFlora: ComponentMapper<FloraComponent>
    private lateinit var mPowerDevice: ComponentMapper<PowerDeviceComponent>
    private lateinit var mPowerConsumer: ComponentMapper<PowerConsumerComponent>
    private lateinit var mPowerGenerator: ComponentMapper<PowerGeneratorComponent>

    lateinit var worldGenJob: ProducerJob<String>

    /**
     * hotspot optimization replaces (amongst other steps) references to entityprocessingsystem with entitysystem.
     * so we can determine this optimization by EntityProcessingSystem missing from our system's hierarchy.
     * this is for artemis-odb optimization, ing annotations. it helps inline some calls
     */
    private
    val isHotspotOptimizationEnabled
            = !ClassReflection.isAssignableFrom(EntityProcessingSystem::class.java, ClientNetworkSystem::class.java)

    //each unit is 1 block(16x16 px), in the game world
    var blocks: ByteArray
    lateinit var assetManager: AssetManager
    lateinit var camera: OrthographicCamera

    //fixme remove in favor of the render system
    lateinit var atlas: TextureAtlas

    var worldGenerator: WorldGenerator? = null

    lateinit var artemisWorld: World
    val worldIO = WorldIO(this)

    lateinit var entityFactory: OreEntityFactory

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

    init {

        //blocks[(x * 2400 + y) << 2 + i] where i = 0, 1, 2 or 3
        //        blocks = new OreBlock[worldSize.height * worldSize.width];
        blocks = ByteArray(worldSize.height * worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT)
    }

    fun init() {
        assert(isHotspotOptimizationEnabled) { "error, hotspot optimization (artemis-odb weaving) is not enabled" }

        if (worldInstanceType == WorldInstanceType.Client || worldInstanceType == WorldInstanceType.ClientHostingServer) {
            initClient()
        } else if (isServer) {
            initServer()
        }

        //        assetManager = new AssetManager();
        //        TextureAtlas blockAtlas = assetManager.get("data/", TextureAtlas.class);
        //        assetManager.finishLoading();
        //        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
    }

    fun initClient() {
        initCamera()

        atlas = TextureAtlas(file("packed/entities.atlas"))

        //note although it may look like it.. order between render and logic ones..actually doesn't matter, their base
        // class dictates this. order between ones of the same type, does though.
        artemisWorld = World(WorldConfigurationBuilder().register(GameLoopSystemInvocationStrategy(msPerLogicTick = 25,
            isServer = false))
            .with(TagManager())
            .with(PlayerManager())
            .with(MovementSystem(this))
            .with(SoundSystem(this))
            .with(ClientNetworkSystem(this))
            .with(InputSystem(camera, this))
            .with(EntityOverlaySystem(this))
            .with(PlayerSystem(this))
            .with(GameTickSystem(this))
            .with(ClientBlockDiggingSystem(this, client!!))
            .with(BackgroundRenderSystem(oreWorld = this, camera = client!!.viewport.camera))
            .with(TileRenderSystem(camera = camera,
                fullscreenCamera = client!!.viewport.camera,
                oreWorld = this))
            .with(SpriteRenderSystem(camera = camera,
                oreWorld = this))
            .with(LiquidRenderSystem(camera = camera, oreWorld = this))
            .with(DebugTextRenderSystem(camera, this))
            .with(PowerOverlayRenderSystem(oreWorld = this,
                fullscreenCamera = client!!.viewport.camera,
                stage = client!!.stage))
            .with(TileTransitionSystem(camera, this))
            .build())
        //b.dependsOn(WorldConfigurationBuilder.Priority.LOWEST + 1000,ProfilerSystem.class);

        //inject the mappers into the world, before we start doing things
        artemisWorld.inject(this, true)


        entityFactory = OreEntityFactory(this)
    }

    fun initServer() {
        artemisWorld = World(WorldConfigurationBuilder()
            .with(TagManager())
            .with(SpatialSystem(this))
            .with(PlayerManager())
            .with(AISystem(this))
            .with(MovementSystem(this))
            .with(ServerPowerSystem(this))
            .with(GameTickSystem(this))
            .with(DroppedItemPickupSystem(this))
            .with(GrassBlockSystem(this))
            .with(ServerNetworkEntitySystem(this))
            .with(ServerBlockDiggingSystem(this))
            .with(PlayerSystem(this))
            .with(ExplosiveSystem(this))
            .with(AirSystem(this))
            .with(ServerNetworkSystem(this, server!!))
            .with(TileLightingSystem(this))
            .with(LiquidSimulationSystem(this))
            .register(GameLoopSystemInvocationStrategy(msPerLogicTick = 25, isServer = true))
            .build())
        //inject the mappers into the world, before we start doing things
        artemisWorld.oreInject(this)

        worldGenerator = WorldGenerator(this)

        entityFactory = OreEntityFactory(this)

        if (OreSettings.flatWorld) {
            worldGenJob = worldGenerator!!.asyncGenerateFlatWorld(worldSize)
            generateWorld()
        } else {
            worldGenJob = worldGenerator!!.asyncGenerateWorld(worldSize)
            generateWorld()
        }

        //severe: obviously...we don't want to do this right after..we can't save the world while we're still generating it
        if (OreSettings.saveLoadWorld) {
            worldIO.saveWorld()
        }
    }

    private fun initCamera() {
        val width = OreSettings.width / BLOCK_SIZE_PIXELS
        val height = OreSettings.height / BLOCK_SIZE_PIXELS
        camera = OrthographicCamera(width, height)//30, 30 * (h / w));
        camera.setToOrtho(true, width, height)
    }

    /**
     * starbound world sizes:
     * smallest: 2048x1504
     * average: 4000x4000
     * large: 6016x4000
     * massive: 8000x4992
     *
     * terraria world sized:
     * small: 4200x1200, sky limit 360-370 above underground level
     * medium: 6400x1800, 450-600 blocks above underground(sky)
     * large: 8400x2400, sky limit: 800-900 blocks above ground
     *
     */
    enum class WorldSize(val width: Int, val height: Int) {
        TestTiny(1024, 1024),
        Smallest(2048, 1500),
        Small(4000, 1500),
        Medium(6400, 1800),
        Large(8400, 2400),
        Huge(8400, 8400)
    }

    private fun generateWorld() {
        //worldGenerator!!.generateWorld(worldSize)
        runBlocking {
            worldGenJob.join()
        }

//        runBlocking {
//            while (true) {
//
//                if (!worldGenJob.isEmpty) {
//                    println("received thing: ${worldGenJob.receive()}")
//                }
//            }
//        }
    }

    /**
     * what an entity's status is, when it comes to
     * lying on solid ground (eg partial, full solid.)
     */
    enum class EntitySolidGroundStatus {
        FullyEmpty,
        PartiallyGrounded,
        FullySolid
    }

    /**
     * checks if the entity's bottom rests entirely on flat/solid ground
     * within a set of X tiles to check.
     *
     * if they're all null, it ignores it (returns true)
     */
    fun isEntityFullyGrounded(entityX: Float,
                              entityY: Float,
                              entityWidth: Float,
                              entityHeight: Float): EntitySolidGroundStatus {
        //fixme to round or truncate, that is the question
        val rightSide = (entityX + (entityWidth * 0.5f)).floor()
        val leftSide = (entityX - (entityWidth * 0.5f)).floor().coerceIn(0, worldSize.width - 10)
        val bottomY = (entityY + (entityHeight * 0.5f)).floor()

        val solidBlocks = mutableListOf<Boolean>()

        (leftSide..rightSide).forEach { tileX -> solidBlocks.add(isBlockSolid(tileX, bottomY)) }

        //all solid,
        if (solidBlocks.all { blockSolid -> blockSolid }) {
            return EntitySolidGroundStatus.FullySolid
        }

        //all empty
        if (solidBlocks.all { blockSolid -> !blockSolid }) {
            return EntitySolidGroundStatus.FullyEmpty
        }

        //some empty
        if (solidBlocks.any { blockSolid -> !blockSolid }) {
            return EntitySolidGroundStatus.PartiallyGrounded
        }

        throw InternalError()
    }

    /**
     * safely return a block at x, y, clamped at world bounds

     * @param x
     * *
     * @param y
     * *
     * *
     * @return
     */
    inline fun blockTypeSafely(x: Int, y: Int): Byte {
        val safeX = x.coerceIn(0, worldSize.width - 1)
        val safeY = y.coerceIn(0, worldSize.height - 1)
        return blocks[(safeX * worldSize.height + safeY) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE]
    }

    /**
     * take a possibly-unsafe x block index,
     * and return a safe (clamped to world size) one

     * @param x
     * *
     * *
     * @return
     */
    inline fun blockXSafe(x: Int): Int {
        return x.coerceIn(0, worldSize.width - 1)
    }

    /**
     * take a possibly-unsafe y block index,
     * and return a safe (clamped to world size) one

     * @param y
     * *
     * *
     * @return
     */
    inline fun blockYSafe(y: Int): Int {
        return y.coerceIn(0, worldSize.height - 1)
    }

    inline fun posXSafe(x: Int): Float {
        return x.coerceIn(0, worldSize.width - 1).toFloat()
    }

    inline fun posYSafe(y: Int): Float {
        return y.coerceIn(0, worldSize.height - 1).toFloat()
    }

    inline fun isWater(x: Int, y: Int): Boolean {
        return blockType(x, y) == OreBlock.BlockType.Water.oreValue
    }

    //blocks[(x * 2400 + y) * 4 + i] where i = 0, 1, 2 or 3
    inline fun blockType(x: Int, y: Int): Byte {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE) {
            "blockType index out of range. x: $x, y: $y"
        }
        */

        //todo can change it to bitshift if we want to...the jvm should already know to do this though..but idk if it
        // will do it
        //blocks[(x * 2400 + y) << 2 + i] where i = 0, 1, 2 or 3
        return blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE]
    }

    fun dumpEntity(entityId: Int) {
        val components = artemisWorld.getComponentsForEntity(entityId)
        val t = components.map { it.printString() }.reduce { acc, s -> acc + "\n$s" }

        val dump = """---- dumping entity... [ entityId: $entityId
$t
]
"""
        println(dump)
    }

    inline fun isInWorldBounds(x: Int, y: Int): Boolean {
        return x >= 0 && y >= 0 && x <= (worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE)
                && y <= (worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE)
    }

    inline fun blockWallType(x: Int, y: Int): Byte {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_WALLTYPE &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_WALLTYPE) {
            "blockWallType index out of range. x: $x, y: $y"
        }
        */

        return blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_WALL_TYPE]
    }

    inline fun blockLightLevel(x: Int, y: Int): Byte {
        return blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_LIGHT_LEVEL]
    }

    inline fun blockMeshType(x: Int, y: Int): Byte {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_MESHTYPE &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_MESHTYPE) {
            "blockMeshType index out of range. x: $x, y: $y"
        }
        */

        return blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_MESHTYPE]
    }

    /**
     * would return range from 1 to 16.
     * in-data it is actually represented as 0-15, but we offset by 1 to make calculations
     * more sane (wouldn't make sense to move over 0 water from one cell to another)
     */
    inline fun liquidLevel(x: Int, y: Int): Byte {
        //hack
        //val level = OreBlock.MAX_LIQUID_LEVEL.toInt().and(0b00001111)
        return blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS]

    }

    /**
     * sets the liquid level but also clears the block type back to Air
     * if it is empty.
     * call this if you are decreasing water, but suspect it will hit 0
     */
    inline fun setLiquidLevelClearIfEmpty(x: Int, y: Int, level: Byte) {
        if (level == 0.toByte()) {
            setBlockType(x, y, OreBlock.BlockType.Air)
        }

        setLiquidLevel(x, y, level)
    }

    /**
     * sets liquid level to @param level and the block type to water.
     * should ONLY BE CALLED WHEN LEVEL IS NOT 0
     */
    inline fun setLiquidLevelWaterNotEmpty(x: Int, y: Int, level: Byte) {
        setBlockType(x, y, OreBlock.BlockType.Water)
        setLiquidLevel(x, y, level)
    }

    //fixme we can mess with using adding bit flags and stuff to them. right now i just have
    inline fun setLiquidLevel(x: Int, y: Int, level: Byte) {
        //val level = OreBlock.MAX_LIQUID_LEVEL.toInt().and(0b00001111)
        //val current = blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS].toInt()

        //the flags to not wipe
        //val upper4Bits = current
        //hack
        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS] = level
    }

    inline fun blockFlags(x: Int, y: Int): Byte {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS) {
            "blockFlags index out of range. x: $x, y: $y"
        }
        */

        return blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS]
    }

    inline fun blockHasFlag(x: Int, y: Int, flag: Byte): Boolean {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS) {
            "blockHasFlag index out of range. x: $x, y: $y"
        }
        */

        return blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS].toInt().and(
            flag.toInt()) != 0
    }

    inline fun setBlockType(x: Int, y: Int, type: OreBlock.BlockType) {
        setBlockType(x, y, type.oreValue)
    }

    inline fun setBlockType(x: Int, y: Int, type: Byte) {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE) {
            "setBlockType index out of range. x: $x, y: $y"
        }
        */

        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_TYPE] = type
    }

    inline fun setBlockWallType(x: Int, y: Int, wallType: Byte) {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_WALLTYPE &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_WALLTYPE) {
            "setBlockWallType index out of range. x: $x, y: $y"
        }
        */

        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_WALL_TYPE] = wallType
    }

    inline fun setBlockMeshType(x: Int, y: Int, meshType: Byte) {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_MESHTYPE &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_MESHTYPE) {
            "setBlockMeshType index out of range. x: $x, y: $y"
        }
        */

        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_MESHTYPE] = meshType
    }

    inline fun setBlockLightLevel(x: Int, y: Int, lightLevel: Byte) {
        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_LIGHT_LEVEL] = lightLevel
    }

    /**
     * overwrites the blocks current flags, to now be the provided flags

     * @param x
     * *
     * @param y
     * *
     * @param flags
     */
    inline fun setBlockFlags(x: Int, y: Int, flags: Byte) {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS) {
            "setBlockFlags index out of range. x: $x, y: $y"
        }
        */

        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS] = flags
    }

    /**
     * disable a block flag

     * @param x
     * *
     * @param y
     * *
     * @param flagToEnable
     */
    inline fun unsetBlockFlag(x: Int, y: Int, flagToEnable: Byte) {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS) {
            "enableBlockFlags index out of range. x: $x, y: $y"
        }
        */

        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS] =
            blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS].toInt().and(
                flagToEnable.toInt()).inv().toByte()
    }

    /**
     * enable a block flag

     * @param x
     * *
     * @param y
     * *
     * @param flagToEnable
     */
    inline fun setBlockFlag(x: Int, y: Int, flagToEnable: Byte) {
        /*
        assert(x >= 0 && y >= 0 &&
                       x <= worldSize.width * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS &&
                       y <= worldSize.height * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS) {
            "enableBlockFlags index out of range. x: $x, y: $y"
        }
        */

        blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS] =
            blocks[(x * worldSize.height + y) * OreBlock.BLOCK_BYTE_FIELD_COUNT + OreBlock.BLOCK_BYTE_FIELD_INDEX_FLAGS].toInt().or(
                flagToEnable.toInt()).toByte()
    }

    /**
     * properly destroys the block in the array (sets meshtype, flags etc to defaults)
     * must be called when destroying a block, so it looks like it was dug.
     */
    inline fun destroyBlock(x: Int, y: Int) {
        setBlockType(x, y, OreBlock.BlockType.Air.oreValue)
        setBlockMeshType(x, y, 0)
        //wall type doesn't get nulled out. i think that's what we want, to preserve underground wall tiles
        setBlockFlags(x, y, 0)
    }

    inline fun isBlockTypeLiquid(type: Byte): Boolean {
        return OreBlock.blockAttributes[type]!!.category == OreBlock.BlockAttributes.BlockCategory.Liquid
    }

    inline fun isBlockLiquid(x: Int, y: Int): Boolean {
        val type = blockType(x, y)
        return OreBlock.blockAttributes[type]!!.category == OreBlock.BlockAttributes.BlockCategory.Liquid
    }

    inline fun isBlockSolid(x: Int, y: Int): Boolean {
        val type = blockType(x, y)

        return OreBlock.blockAttributes[type]!!.collision == OreBlock.BlockAttributes.Collision.True
    }

    fun entityAtPosition(pos: Vector2): Int? {
        val entities = artemisWorld.entities(allOf(SpriteComponent::class)).toMutableList()

        for (currentEntity in entities) {
            //could be placement overlay, but we don't want this. skip over.
            if (shouldIgnoreClientEntityTag(currentEntity)) {
                continue
            }

            val cSprite = mSprite.get(currentEntity)
            if (cSprite.sprite.rect.contains(pos)) {
                return currentEntity
            }
        }

        return null
    }

    /**
     * @return true if the entity tag should be ignore for attacks and stuff.
     * excludes stuff like main player, item overlays, crosshairs, for the client...
     */
    fun shouldIgnoreClientEntityTag(entity: Int, ignoreOwnPlayer: Boolean = true): Boolean {
        val tag = tagManager.opt(entity) ?: return false

        when {
            tag == OreWorld.s_itemPlacementOverlay -> return true
            tag == OreWorld.s_crosshair -> return true
            tag == OreWorld.s_mainPlayer && ignoreOwnPlayer -> return true
            else -> return false
        }
    }

    /**
     * Safely shutdown the world, disposing of all the systems
     * Each system should be designed such that it can safely shut itself down without
     * having to interface with other systems. Though some exceptions may apply
     */
    fun shutdown() {
        logger.debug { "shutdown..." }
        artemisWorld.dispose()
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

    /**
     * Attempts to place a block at position with the type, can fail. If it succeeds it will *not*
     * notify anything (network wise). If it succeeds, it will take care of destroying e.g. nearby grass,
     * and doing whatever else may need to be done on nearby conditions

     * @param x
     * *
     * @param y
     * *
     * @param placedBlockType
     * *         block type to change it to
     * *
     * *
     * @return true if placement succeeded.
     */
    fun attemptBlockPlacement(x: Int, y: Int, placedBlockType: Byte): Boolean {
        val blockType = blockTypeSafely(x, y)
        val blockLiquid = isBlockTypeLiquid(blockType)

        //attempt to place one if the area is empty
        if (blockType == OreBlock.BlockType.Air.oreValue || blockLiquid) {
            if (blockLiquid) {
                //unset liquid levels! he's placing a block on top of water
                //so water should now disappear
                setLiquidLevel(x, y, 0)
            }

            setBlockType(x, y, placedBlockType)

            val bottomBlockX = x
            val bottomBlockY = y + 1
            if (blockHasFlag(bottomBlockX, bottomBlockY, OreBlock.BlockFlags.GrassBlock)) {
                //remove grass flag here.
                unsetBlockFlag(bottomBlockX, bottomBlockY, OreBlock.BlockFlags.GrassBlock)
            }

            return true
        }
        //TODO: check collision with other entities...

        return false
    }

    fun mousePositionWorldCoords(): Vector2 {
        //libgdx can and probably will return negative mouse coords..
        return screenToWorldCoords(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
    }

    fun screenToWorldCoords(x: Float, y: Float): Vector2 {
        // we ensure it is within bounds of screen (mouse pos can be negative sometimes, oddly)
        val input = Vector3((x).coerceAtLeast(0f), (y).coerceAtLeast(0f), 0f)
        val worldCoords = camera.unproject(input)

        return Vector2(worldCoords.x, worldCoords.y)
    }

    /**
     * @param pos the pos that will get modified at return
     * @param size of the entity
     */
    fun alignPositionToBlocks(pos: Vector2, size: Vector2) {
        var x = (pos.x).floorf()
        var y = (pos.y).floorf()

        //if size is odd,  it won't look aligned properly
        if (size.x % 2 == 1f) {
            x -= 0.5f
        }

        //odd
        if (size.y % 2 == 1f) {
            y -= 0.5f
        }

        pos.set(x, y)
    }

    fun seaLevel(): Int {
        return WORLD_SEA_LEVEL
    }

    /**
     * finds nearest y point to spawn at, for
     * e.g. player spawning on the ground without falling to his death
     * @return y position that is just before the solid (above)
     */
    fun findSolidGround(x: Float): Float {
        for (y in 0 until worldSize.height) {
            if (isBlockSolid(x.toInt(), y)) {
                //return first solid ground we find
                return y - 1f
            }
        }

        error("couldn't find a place to spawn this?")
    }

    /**
     * @param entity
     * *         entity id
     * *
     * *
     * @return true if the item can be placed where it currently resides, without any obstructions
     */
    fun isPlacementValid(entity: Int): Boolean {
        return true
        val cSprite = mSprite.get(entity)
        val pos = Vector2(cSprite.sprite.x, cSprite.sprite.y)
        val size = Vector2(cSprite.sprite.width, cSprite.sprite.height)

        val epsilon = 0.001f
        val startX = (pos.x - size.x * 0.5f + epsilon).toInt()
        val startY = (pos.y - size.y * 0.5f + epsilon).toInt()

        val endX = (pos.x + size.x * 0.5f).toInt()
        val endY = (pos.y + (size.y * 0.5f - epsilon) + 1).toInt()

        if (!(startX == blockXSafe(startX) && startY == blockYSafe(startY) && endX == blockXSafe(endX)
                    && endY == blockYSafe(endY))) {
            //fixme
            //not sure why, but this ends up giving me some way way invalid values. likely due to mouse being outside
            //of valid range, *somehow*. sometimes does it on startup etc
            return false
        }

        if (isBlockRangeSolid(startX, endX, startY, endY)) {
            //check collision against blocks first, make sure where the object is getting placed,
            //there are no blocks obstructing beneath it. if there are, fail the placement
            return false
        }

        //ensure placement meets the adjacency hints (like "must be connected to
        //a solid block on the top", etc)
        mItem.ifPresent(entity) { cItem ->
            if (!placementAdjacencyHintsBlocksSatisfied(entity, cItem)) {
                return false
            }
        }

        //check collision against entities
        val entities = artemisWorld.entities(allOf(SpriteComponent::class)).toMutableList()

        for (currentEntity in entities) {
            //it's the item we're trying to place, don't count a collision with ourselves
            if (currentEntity == entity) {
                continue
            }

            isItemDroppedInWorldOpt(currentEntity)

            val entitySpriteComponent = mSprite.get(currentEntity)
            // possible colliding object is not meant to be collided with. skip it/don't count it
            if (entitySpriteComponent.noClip) {
                continue
            }

            if (entityCollides(currentEntity, entity)) {
                return false
            }
        }

        return true
    }

    /**
     * returns if this item is dropped in the world,
     * or if it is not an item at all!
     */
    fun isItemDroppedInWorldOpt(entityId: Int): Boolean {
        mItem.ifPresent(entityId) {
            if (it.state == ItemComponent.State.DroppedInWorld) {
                return true
            }
        }

        return false
    }

    /**
     * @returns true if this item is placed or moving in the world
     * or false otherwise. or false if it's not an item
     */
    fun isItemPlacedInWorldOpt(entityId: Int): Boolean {
        mItem.ifPresent(entityId) {
            if (it.state == ItemComponent.State.InWorldState) {
                return true
            }
        }

        return false
    }

    fun placementAdjacencyHintsBlocksSatisfied(entityId: Int, cItem: ItemComponent): Boolean {
        val cSprite = mSprite.get(entityId)
        val left = cSprite.sprite.rect.left.toInt()
        val right = cSprite.sprite.rect.right.toInt()
        val top = cSprite.sprite.rect.top.toInt() - 1
        val bottom = cSprite.sprite.rect.bottom.toInt() + 1

        //for each (if any) placement requirement, ensure it was satisfied
        if (ItemComponent.PlacementAdjacencyHints.TopSolid in cItem.placementAdjacencyHints) {
            if (!isBlockRangeSolid(left, right, top, top)) {
                return false
            }
        }

        if (ItemComponent.PlacementAdjacencyHints.BottomSolid in cItem.placementAdjacencyHints) {
            if (!isBlockRangeSolid(left, right, bottom, bottom)) {
                return false
            }
        }

        return true
    }

    /**
     * checks all blocks in given range for solidity.
     */
    fun isBlockRangeSolid(startX: Int, endX: Int, startY: Int, endY: Int): Boolean {
        for (x in startX..endX) {
            for (y in startY..endY) {
                if (!isBlockSolid(x, y)) {
                    return false
                }
            }
        }

        return true
    }

    private fun entityCollides(first: Int, second: Int): Boolean {
        val spriteComponent1 = mSprite.get(first)
        val spriteComponent2 = mSprite.get(second)

        val pos1 = Vector2(spriteComponent1.sprite.x, spriteComponent1.sprite.y)
        val pos2 = Vector2(spriteComponent2.sprite.x, spriteComponent2.sprite.y)

        val size1 = Vector2(spriteComponent1.sprite.width, spriteComponent1.sprite.height)
        val size2 = Vector2(spriteComponent2.sprite.width, spriteComponent2.sprite.height)

        val epsilon = 0.0001f

        val left1 = pos1.x - size1.x * 0.5f + epsilon
        val right1 = pos1.x + size1.x * 0.5f - epsilon
        val top1 = pos1.y - size1.y * 0.5f + epsilon
        val bottom1 = pos1.y + size1.y * 0.5f - epsilon

        val left2 = pos2.x - size2.x * 0.5f + epsilon
        val right2 = pos2.x + size2.x * 0.5f - epsilon

        val top2 = pos2.y - size2.y * 0.5f + epsilon
        val bottom2 = pos2.y + size2.y * 0.5f - epsilon

        val collides = !(left2 > right1 || right2 < left1 || top2 > bottom1 || bottom2 < top1)

        return collides
    }

    fun loadSparseBlockUpdate(update: Network.Shared.SparseBlockUpdate) {
        //log("sparse block update", "loaded, count: " + update.blocks.size);

        for (sparseBlock in update.blocks) {
            sparseBlock.apply {
                setBlockType(x, y, block.type)
                setBlockWallType(x, y, block.wallType)
                setBlockFlags(x, y, block.flags)
            }
        }
    }

    /**
     * Clone everything about the entity and adds it to the engine/world

     * @param sourceEntity
     * *         to clone
     * *
     * *
     * @return the cloned entity
     */
    fun cloneEntity(sourceEntity: Int): Int {
        val clonedEntity = artemisWorld.create()

        //sorted alphabetically for your pleasure
        mAir.ifPresent(sourceEntity) {
            val component = mAir.create(clonedEntity)
            component.copyFrom(it)
        }

        mBlock.ifPresent(sourceEntity) {
            val component = mBlock.create(clonedEntity)
            component.copyFrom(it)
        }

        mControl.ifPresent(sourceEntity) {
            val component = mControl.create(clonedEntity)
            component.copyFrom(it)
        }

        mFlora.ifPresent(sourceEntity) {
            val component = mFlora.create(clonedEntity)
            component.copyFrom(it)
        }

        mHealth.ifPresent(sourceEntity) {
            val component = mHealth.create(clonedEntity)
            component.copyFrom(it)
        }

        mItem.ifPresent(sourceEntity) {
            val component = mItem.create(clonedEntity)
            //fixme for first execution of this, it takes ~400ms which is crazy
            //it is literally ONLY this one, for a generator. not sure why yet.
            component.copyFrom(it)
        }

        mJump.ifPresent(sourceEntity) {
            val component = mJump.create(clonedEntity)
            component.copyFrom(it)
        }

        //player, unneeded
        assert(mPlayer.opt(sourceEntity) == null)

        mSprite.ifPresent(sourceEntity) {
            val component = mSprite.create(clonedEntity)
            component.copyFrom(it)

            if (!isServer) {
                logger.debug { "client entity cloner, sprite textturename: ${component.textureName.toString()}" }
                component.sprite.setRegion(atlas.findRegion(component.textureName))
            }
        }

        mTool.ifPresent(sourceEntity) {
            val component = mTool.create(clonedEntity)
            component.copyFrom(it)
        }

        mDoor.ifPresent(sourceEntity) {
            val component = mDoor.create(clonedEntity)
            component.copyFrom(it)
        }

        mLight.ifPresent(sourceEntity) {
            val component = mLight.create(clonedEntity)
            component.copyFrom(it)
        }

        mVelocity.ifPresent(sourceEntity) {
            val component = mVelocity.create(clonedEntity)
            component.copyFrom(it)
        }

        mPowerDevice.ifPresent(sourceEntity) {
            val component = mPowerDevice.create(clonedEntity)
            component.copyFrom(it)
        }

        mPowerConsumer.ifPresent(sourceEntity) {
            val component = mPowerConsumer.create(clonedEntity)
            component.copyFrom(it)
        }

        mPowerGenerator.ifPresent(sourceEntity) {
            val component = mPowerGenerator.create(clonedEntity)
            component.copyFrom(it)
            component.fuelSources = GeneratorInventory(GeneratorInventory.MAX_SLOTS, artemisWorld)

            //hack until we come up with a better way, possibly pass world in
            //as copy param, to get these injected well
            artemisWorld.inject(component.fuelSources, true)
        }

        return clonedEntity
    }

    /**
     * gets the player entity that corresponds to this player connection id.

     * @param playerId
     * *         the connection playerid of the player
     * *
     * *
     * @return the player entity
     */
    fun playerEntityForPlayerConnectionID(playerId: Int): Int {
        var cPlayer: PlayerComponent
        for (player in players()) {
            cPlayer = mPlayer.get(player)

            if (cPlayer.connectionPlayerId == playerId) {
                return player
            }
        }

        throw IllegalStateException("player id attempted to be obtained from world, but this player does not exist")
    }

    fun players(): List<Int> {
        return artemisWorld.entities(allOf(PlayerComponent::class)).toMutableList()
    }

    //fixme better way to do key and mouse events. i'd like to just have systems be able to sign up,
//and they can process that in there. or maybe this should be in the client..after all, a server has no key events
    fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    @Suppress("unused")
    inline fun <reified T : Component> getEntitiesWithComponent(): IntBag {
        return artemisWorld.entities(allOf(T::class))
    }

    val fboDumpTimer = OreTimer().apply { reset() }
    fun dumpFboAndExitAfterMs(ms: Long = 5000) {
        if (fboDumpTimer.surpassed(ms)) {
            dumpFbo()
            System.exit(0)
        }
    }

    fun dumpFboAfterMs(ms: Long = 5000) {
        if (fboDumpTimer.surpassed(ms)) {
            dumpFbo()
        }
    }

    fun dumpFbo() {
        val pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.backBufferWidth, Gdx.graphics.backBufferHeight)
        val file = FileHandle("../saveData/debug/framebufferdump.png")
        val png = PixmapIO.PNG().apply {
            setFlipY(true)
        }

        png.write(file, pixmap)
    }

    /**
     * Killing of entity.
     *
     * does the appropriate server-side action when an entity gets killed or destroyed
     * (e.g. triggering explosions and so on.)
     *
     * Destruction could happen for various reasons, could whether player caused (rabbit killed by player)
     *
     *
     * Server-side only. client will not call this.
     *
     * @param entityToKill entity id to kill and perform proper death logic
     * @param entityKiller entity id of the one who killed them. usually
     * this would be a player. but it could be just about anything else,
     * or nothing (e.g. if something just died by itself)
     */
    fun killEntity(entityToKill: Int, entityKiller: Int) {
        val itemComp = mItem.opt(entityToKill)

        mFlora.ifPresent(entityToKill) { cFlora ->
            killTree(cFlora, entityToKill, entityKiller)
        }

        artemisWorld.delete(entityToKill)

        artemisWorld.system<ServerNetworkSystem>().sendEntityKilled(entityToKill)
    }

    /**
     * server side ONLY
     * Destroys an entity in the world, "silently". This is used for example,
     * when items get picked up or disappear. In these cases they are not getting
     * killed.
     */
    fun serverDestroyEntity(entityToDestroy: Int) {
        artemisWorld.delete(entityToDestroy)

        artemisWorld.system<ServerNetworkSystem>().sendEntityKilled(entityToDestroy)
    }

    /**
     * usable by either client/server. destroys entity in the world
     * silently; no sound, no server -> client informing
     * NOTE: ONLY FOR client, because it won't indicate that it was destroyed
     */
    fun destroyEntity(entityToDestroy: Int) {
        artemisWorld.delete(entityToDestroy)
    }

    fun destroyEntities(entityIds: List<Int>) {
        for (entityToDestroy in entityIds) {
            artemisWorld.delete(entityToDestroy)
        }
    }

    private fun killTree(cFlora: FloraComponent, entityToKill: Int, entityKiller: Int?) {
        //this behavior is for exploding flora into a bunch of dropped items
        //for example, when destroying a tree in games like terraria, it gives
        //a satisfying exploding of dropped items
        for (i in 0..cFlora.numberOfDropsWhenDestroyed) {
            //todo actually what we want is not to clone, but to drop wood.
            //same for rubber trees. but they may also drop a sapling
            val cloned = cloneEntity(entityToKill)
            val clonedSpriteComp = mSprite.get(cloned)
            val random = RandomXS128()
            clonedSpriteComp.sprite.apply {
                x += random.nextInt(0, 5)
                y += random.nextInt(0, 5)
            }

            val clonedItemComp = mItem.get(cloned).apply {
                stackSize = cFlora.stackSizePerDrop
                state = ItemComponent.State.DroppedInWorld
                //half the size, it's a dropped tree
                //hack

                //fixme functionalize this, duplicated of/by networkserversystem drop request
                sizeBeforeDrop = Vector2(clonedSpriteComp.sprite.width,
                    clonedSpriteComp.sprite.height)
                timeOfDropMs = TimeUtils.millis()
            }

            val reducedWidth = (clonedSpriteComp.sprite.width * 0.25f)
            val reducedHeight = (clonedSpriteComp.sprite.height * 0.25f)
            //shrink the size of all dropped items, but also store the original size first, so we can revert later
            clonedSpriteComp.sprite.setSize(reducedWidth, reducedHeight)

            //                    sizeBeforeDrop

            mSprite.get(cloned).apply {
            }

            //                mSprite.get(cloned).apply {
            //                   sprite.setPosition()
            //              }
            //todo give it some explody velocity
        }
    }

    /**
     * debug
     */
    fun printLiquidLevels(startX: Int, startY: Int, endX: Int, endY: Int) {
        //ascii print of the world liquid levels
        for (y in startY until endY) {
            for (x in startX until endX) {
                var s = "  |  "
                if (isBlockLiquid(x, y)) {
                    s += liquidLevel(x, y).toString()
                } else {
                    val blockType = blockType(x, y)
                    s += when (blockType) {
                        OreBlock.BlockType.Air.oreValue -> "A"
                        OreBlock.BlockType.Dirt.oreValue -> "D"
                        else -> "?"
                    }
                }

                print(s)
            }
            println()
        }
    }

    fun debugFirstPlayerPosition(): Vector2 {
        val cSprite = mSprite.get(players().first())
        return Vector2(cSprite.sprite.x, cSprite.sprite.y)
    }

    /**
     * @return true if the entityId is able to be attacked.
     *
     * e.g. items dropped in the world are not attackable
     */
    fun canAttackEntity(entityId: Int): Boolean {
        mItem.ifPresent(entityId) { cItem ->
            if (cItem.state == ItemComponent.State.DroppedInWorld) {
                return false
            }
        }
        //don't let them attack dropped items, makes no sense

        return true
    }
}



