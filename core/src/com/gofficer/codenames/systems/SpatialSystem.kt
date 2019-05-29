package com.gofficer.codenames.systems

import com.artemis.Aspect
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.CardComponent
import com.gofficer.codenames.components.TransformComponent
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.systems.server.ServerNetworkSystem
import com.gofficer.codenames.utils.mapper
import com.gofficer.codenames.utils.require
import com.gofficer.codenames.utils.system
import net.mostlyoriginal.api.utils.QuadTree

@Wire(failOnNull = false)
/**
 * system for keeping track of which entities should be on each
 * and every player/client,

 * for now this is only used by the server, so assumptions can be made based on that.
 */
class SpatialSystem(private val gameWorld: GameWorld) : IteratingSystem(Aspect.all()) {

    private val mCard by require<CardComponent>()
    private val mTransform by mapper<TransformComponent>()
    // private val mPlayer by mapper<PlayerComponent>()
    // private val mControl by mapper<ControllableComponent>()
//    private val mItem by mapper<ItemComponent>()
    // private val mVelocity by mapper<VelocityComponent>()
    // private val mJump by mapper<JumpComponent>()

    private val serverNetworkSystem by system<ServerNetworkSystem>()

    var quadTree: QuadTree

    init {
        quadTree = QuadTree(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT)
    }

    override fun removed(entityId: Int) {
        quadTree.remove(entityId)
    }

    override fun inserted(entityId: Int) {
//
//        // ignore things in an inventory
//        mItem.ifPresent(entityId) {
//            if (it.state == ItemComponent.State.InInventoryState)
//                return@inserted
//        }

        val cCard = mCard.get(entityId)
        val cTransform = mTransform.get(entityId).velocity
        quadTree.insert(entityId, cTransform.x, cTransform.y,
            100f, 100f)
    }

    override fun process(entityId: Int) {

        // ignore things in an inventory
//        mItem.ifPresent(entityId) {
//            if (it.state == ItemComponent.State.InInventoryState)
//                return@process
//        }

        val cCard = mCard.get(entityId)
        val cTransform = mTransform.get(entityId).velocity
//        val cSprite = mSprite.get(entityId)
        quadTree.update(entityId, cTransform.x, cTransform.y,
            100f, 100f)
    }
}