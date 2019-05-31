package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Exclude
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.components.*
import com.gofficer.codenames.event.TouchEvent
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.mapper
import ktx.log.logger
import net.mostlyoriginal.api.event.common.Subscribe
import java.util.concurrent.ConcurrentLinkedQueue

@Wire
@All(TextureRenderableComponent::class, PositionComponent::class)
@Exclude(RevealedComponent::class)
class CardPressedSystem : IteratingSystem() {

    private lateinit var mPosition: ComponentMapper<PositionComponent>
    private val mRevealed by mapper<RevealedComponent>()

    private lateinit var clientNetworkSystem: ClientNetworkSystem

    private val touchEventQueue = ConcurrentLinkedQueue<Vector2>()

    override fun process(entityId: Int) {
        if (touchEventQueue.peek() != null) {
//            log.debug { "Processing $entityId"}
            val position = mPosition.get(entityId)
            // TODO update bounds to be dynamic
            val bounds = Rectangle(position.x, position.y, 150f, 100f)
            val touchEvent = touchEventQueue.peek()
//            val touchEvent = touchEventQueue.poll()
            if (bounds.contains(touchEvent.x, touchEvent.y)) {
                log.debug { "Touched entity $entityId"}
//                mRevealed.set(entityId, true)
                // TODO add flip animation
                // TODO add network component?
                clientNetworkSystem.sendCardTouched(entityId)
            }
        }
    }

    override fun end() {
        super.end()
        touchEventQueue.poll()
    }

    companion object {
        val log = logger<CardPressedSystem>()
    }

    @Subscribe
    fun customListener(event: TouchEvent) {
        touchEventQueue.add(Vector2(event.x, event.y))
        log.debug { "Received touch event ${event.x}, ${event.y}"}
    }
}