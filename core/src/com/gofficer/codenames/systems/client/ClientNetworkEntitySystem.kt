package com.gofficer.codenames.systems.client

import com.artemis.Aspect
import com.artemis.Component
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.artemis.utils.Bag
import com.badlogic.gdx.utils.Array
import com.gofficer.codenames.components.*
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.systems.server.NetworkManager
import com.gofficer.codenames.systems.server.ServerNetworkSystem
import com.gofficer.codenames.utils.mapper
import com.gofficer.codenames.utils.system
import com.gofficer.codenames.utils.toMutableList
import ktx.log.logger


@All(NetworkComponent::class, DirtyComponent::class)
class ClientNetworkEntitySystem() : IteratingSystem() {

    lateinit var clientNetworkSystem: ClientNetworkSystem

    companion object {
        val log = logger<ClientNetworkEntitySystem>()
        val INVALID_ENTITY_ID = -1
    }

    private val mDirty by mapper<DirtyComponent>()

    override fun process(entityId: Int) {

        // send then remove
        mDirty.set(entityId, false)
        clientNetworkSystem.kryonetClient.sendToAll(EntityUpdate(entityId, serializeComponents(entityId)))
    }

    // TODO align with server and clinet in one spot?
    private fun serializeComponents(entityId: Int): Array<Component> {
        val components = Bag<Component>()

        world.getEntity(entityId).getComponents(components)

        val copyComponents = Array<Component>()
        for (component in components) {
            assert(component != null) {
                "component in list of components for entity was null somehow. shouldn't be possible"
            }


            when (component) {
                is PlayerComponent -> {
                    //skip
                }
                // TODO drive by annotation instead
                is TextureRenderableComponent -> {}
//                is SpriteComponent -> {
//                    //skip
//                }
//                is ControllableComponent -> {
//                    //skip
//                }
                else -> copyComponents.add(component)
            }
        }

        return copyComponents
    }

}