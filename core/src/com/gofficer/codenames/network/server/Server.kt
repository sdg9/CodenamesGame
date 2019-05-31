package com.gofficer.codenames.network.server

import com.artemis.BaseSystem
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.PlayerManager
import com.artemis.managers.TagManager
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.gofficer.codenames.Network
import com.gofficer.codenames.systems.RemoveSystem
import com.gofficer.codenames.systems.SpatialSystem
import com.gofficer.codenames.systems.server.NetworkManager
import com.gofficer.codenames.systems.server.ServerNetworkEntitySystemOld
import com.gofficer.codenames.systems.server.ServerNetworkSystem
import com.gofficer.codenames.systems.server.ServerNetworkSystemOld
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetServerMarshalStrategy
import java.util.HashMap

//
//class Server(
////    private val objectManager: ObjectManager,
////    private val spellManager: SpellManager,
////    private val maps: HashMap<Int, Map>
//) {
//    var world: World? = null
//        private set
//    private var strategy: KryonetServerMarshalStrategy? = null
//    private var players: Set<Network.Shared.Player>? = null
////
////    val itemManager: ItemManager
////        get() = getManager(ItemManager::class.java)
////
////    val mapManager: MapManager
////        get() = getManager(MapManager::class.java)
////
////    val worldManager: WorldManager
////        get() = getManager(WorldManager::class.java)
//
//    val networkManager: NetworkManager
//        get() = getManager(NetworkManager::class.java)
//
////    val combatManager: CombatSystem
////        get() = getManager(PhysicalCombatSystem::class.java)
////
////    val magicCombatManager: MagicCombatSystem
////        get() = getManager(MagicCombatSystem::class.java)
//
//    init {
//        create()
//    }
//
//    fun create() {
//        initWorld()
//        createMap()
//        createWorld()
//    }
//
//    private fun initWorld() {
//        println("Initializing systems...")
//        val builder = WorldConfigurationBuilder()
////        strategy = KryonetServerMarshalStrategy(tcpPort, udpPort)
//        val myStrategy = KryonetServerMarshalStrategy("127.0.0.1", Network.PORT)
//        this.strategy = myStrategy
//        builder
//            .with(
//                ServerNetworkSystem(this, myStrategy)
////                TagManager(),
////                SpatialSystem(this),
////                PlayerManager(),
////                ServerNetworkEntitySystemOld(server!!),
////                ServerNetworkSystemOld(this, server!!),
////                RemoveSystem()
//            )
//
////            .with(FluidEntityPlugin())
////            .with(ServerSystem(this, strategy))
////            .with(NetworkManager(this, strategy))
////            .with(ItemManager(this))
////            .with(NPCManager())
////            .with(MapManager(this, maps))
////            .with(spellManager)
////            .with(objectManager)
////            .with(PathFindingSystem(PATH_FINDING_INTERVAL))
////            .with(NPCAttackSystem(NPC_ATTACK_INTERVAL))
////            .with(WorldManager(this))
////            .with(PhysicalCombatSystem(this))
////            .with(MagicCombatSystem(this))
////            .with(EnergyRegenerationSystem(ENERGY_REGENERATION_INTERVAL))
////            .with(MeditateSystem(this, MEDITATE_INTERVAL))
////            .with(FootprintSystem(this, FOOTPRINT_LIVE_TIME))
////            .with(RandomMovementSystem(this))
////            .with(BuffSystem())
//        world = World(builder.build())
////        world!!.getSystem(MapManager::class.java).postInitialize()
//        println("WORLD CREATED")
//    }
//
//
//    private fun createWorld() {
//        // testing
//    }
//
//    private fun createMap() {
//
//    }
//
//    fun update() {
//        world!!.setDelta(MathUtils.clamp(Gdx.graphics.deltaTime, 0f, 1 / 16f))
//        world!!.process()
//    }
//
//    internal fun addPlayers(players: Set<Network.Shared.Player>) {
//        this.players = players
//    }
//
//    private fun <T : BaseSystem> getManager(managerType: Class<T>): T {
//        return world!!.getSystem(managerType)
//    }
////
////    fun getSpellManager(): SpellManager {
////        return getManager(SpellManager::class.java)
////    }
////
////    fun getObjectManager(): ObjectManager {
////        return getManager(ObjectManager::class.java)
////    }
//}
