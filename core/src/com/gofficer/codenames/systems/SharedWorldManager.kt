package com.gofficer.codenames.systems

import com.artemis.BaseSystem
import com.artemis.Entity
import com.artemis.World
import java.util.*
import com.artemis.ComponentMapper
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.NetworkComponent
import com.gofficer.codenames.components.PositionComponent


class SharedWorldManager(val gameWorld: GameWorld) : BaseSystem() {

//    private lateinit var mPosition: ComponentMapper<PositionComponent>

    private lateinit var mNetwork: ComponentMapper<NetworkComponent>


    override fun processSystem() {
    }

    private val networkedEntities = HashMap<Int, Int>()

    val entities: Set<Int>
        get() = HashSet(networkedEntities.values)

//    val world: World
//        get() = GameScreen.getWorld()

    fun entityExsists(networkId: Int): Boolean {
        return networkedEntities.containsKey(networkId)
    }

    fun getNetworkedEntity(networkId: Int): Int? {
        return networkedEntities[networkId]
    }

    fun hasNetworkedEntity(networkId: Int): Boolean {
        return networkedEntities.containsKey(networkId)
    }

    fun registerEntity(networkId: Int, entityId: Int) {
        mNetwork.create(entityId).apply {
            id = networkId
        }
//        Entity(entityId)
//        E(entityId).network().getNetwork().id = networkId
        networkedEntities[networkId] = entityId
    }

    fun unregisterEntity(networkId: Int) {
        val entityId = networkedEntities[networkId]
        if (entityId != null) {
            world.delete(entityId)
            networkedEntities.remove(networkId)
        }
    }

    fun getNetworkedId(id: Int): Optional<Int> {
        return networkedEntities
            .entries
            .stream()
            .filter { entry -> entry.value === id }
            .map { it.key }
//            .map(???({ it.key }))
        .findFirst()
    }
}
