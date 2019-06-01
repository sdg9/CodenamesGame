package com.gofficer.codenames.systems.client

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Exclude
import com.artemis.annotations.Wire
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.gofficer.codenames.components.*
import com.gofficer.codenames.event.TouchEvent
import com.gofficer.codenames.utils.mapper
import ktx.log.logger
import net.mostlyoriginal.api.event.common.Subscribe
import java.util.concurrent.ConcurrentLinkedQueue

@Wire
@All(TextureRenderableComponent::class, PositionComponent::class)
@Exclude(RevealedComponent::class)
class CardPressedSystem : IteratingSystem() {

    private lateinit var mPosition: ComponentMapper<PositionComponent>
    private lateinit var mRectangle: ComponentMapper<RectangleComponent>
    private lateinit var mFlipAnimation: ComponentMapper<FlipAnimationComponent>
    private val mRevealed by mapper<RevealedComponent>()
    private val mDirty by mapper<DirtyComponent>()

    private val touchEventQueue = ConcurrentLinkedQueue<Vector2>()

    override fun process(entityId: Int) {
        if (touchEventQueue.peek() != null) {
//            log.debug { "Processing $entityId"}
            val position = mPosition.get(entityId)
            val rectangle = mRectangle.get(entityId)

            val bounds = Rectangle(position.x, position.y, rectangle.width, rectangle.height)
            val touchEvent = touchEventQueue.peek()
//            val touchEvent = touchEventQueue.poll()
            if (bounds.contains(touchEvent.x, touchEvent.y)) {
                log.debug { "Touched entity $entityId"}
                // TODO optimized approach would be to auto set dirty when I change revealed (or any other component)
                // as well as only pass in the changed components
                // Currently I must also say component is dirty (not synced w/ network) and it passes the component in its entirety
                mRevealed.set(entityId, true)
                // Dirty is processed by client's network sync system
                mDirty.set(entityId, true)
                // TODO WIP on animation
                mFlipAnimation.create(entityId)
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