package com.gofficer.codenames.network.notification

import com.artemis.Component
import com.gofficer.codenames.network.interfaces.INotification
import com.gofficer.codenames.network.interfaces.INotificationProcessor
import java.util.*


class EntityUpdate(var entityId: Int = -1, var components: Array<Component> = arrayOf(), var toRemove: Array<Class<*>>? = arrayOf()) : INotification {

//    var entityId: Int = -1
//    lateinit var components: Array<Component>
//    lateinit var toRemove: Array<Class<*>>
//
//    constructor() {}
//
//    constructor(entityId: Int, components: Array<Component>, toRemove: Array<Class<*>>) {
//        this.entityId = entityId
//        this.components = components
//        this.toRemove = toRemove
//    }

    override fun accept(processor: INotificationProcessor) {
        processor.processNotification(this)
    }

    class EntityUpdateBuilder {

        private var entityUpdate: EntityUpdate? = null
        private val components = HashSet<Component>()
        private val toRemove = HashSet<Class<*>>()

        val isEmpty: Boolean
            get() = components.isEmpty() && toRemove.isEmpty()

        fun withComponents(vararg components: Component): EntityUpdateBuilder {
            this.components.addAll(Arrays.asList(*components))
            return this
        }

        fun remove(vararg toRemove: Class<*>): EntityUpdateBuilder {
            this.toRemove.addAll(Arrays.asList<Class<*>>(*toRemove))
            return this
        }

        fun build(): EntityUpdate {
            entityUpdate!!.components = components.toTypedArray()
            entityUpdate!!.toRemove = toRemove.toTypedArray()
            return entityUpdate!!
        }

        companion object {

            fun of(entityId: Int): EntityUpdateBuilder {
                val builder = EntityUpdateBuilder()
                builder.entityUpdate = EntityUpdate()
                builder.entityUpdate!!.entityId = entityId
                return builder
            }
        }
    }
}