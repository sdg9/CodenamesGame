package com.gofficer.codenames.systems.client

import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Gdx
import com.artemis.Aspect
import com.artemis.ComponentMapper
import com.artemis.annotations.Wire
import com.artemis.managers.TagManager
import com.artemis.systems.EntityProcessingSystem
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Input
import com.gofficer.codenames.components.Clickable
import ktx.log.logger
import net.mostlyoriginal.api.component.basic.Bounds
import net.mostlyoriginal.api.system.physics.CollisionSystem

/**
 * Track mouse over clickables. will indicate hover or clicked.
 *
 * @author Daan van Yperen
 */


@Wire
class MouseClickSystem : IteratingSystem(Aspect.all(Clickable::class.java, Bounds::class.java)) {
    companion object {
        val log = logger<MouseClickSystem>()
    }
    internal var system: CollisionSystem? = null
    internal var tagManager: TagManager? = null

    protected var mClickable: ComponentMapper<Clickable>? = null
    private var leftMousePressed: Boolean = false

    override fun begin() {
        super.begin()

        leftMousePressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT)
    }

    override fun process(id: Int) {
        val e = world.getEntity(id)
        log.debug { "Mouse click!"}
        val cursor = tagManager!!.getEntity("cursor")
        if (cursor != null) {
            // update state based on cursor.
            val clickable = mClickable!!.get(e)
            val overlapping = system!!.overlaps(cursor, e)
            if (overlapping) {
                clickable.state = if (leftMousePressed) Clickable.ClickState.CLICKED else Clickable.ClickState.HOVER
            } else {
                clickable.state = Clickable.ClickState.NONE
            }
        }
    }
}