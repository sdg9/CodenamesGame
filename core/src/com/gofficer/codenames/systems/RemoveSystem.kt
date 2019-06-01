package com.gofficer.codenames.systems

import com.artemis.annotations.One
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.gofficer.codenames.components.RemoveComponent

@Wire
@One(RemoveComponent::class)
class RemoveSystem: IteratingSystem() {
    override fun process(entityId: Int) {
        // TODO should all components be removed first?
        world.delete(entityId)
    }

}