package com.gofficer.codenames

import com.artemis.Component
import net.mostlyoriginal.api.network.marshal.common.MarshalDictionary
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque


class NetworkDictionary : MarshalDictionary() {
    init {
        registerAll(



            // Game Requests
            Array<Any>::class.java,
            // Other
            Array<BooleanArray>::class.java,
            BooleanArray::class.java,
            Array<IntArray>::class.java,
            IntArray::class.java,
            Array<Int>::class.java,
            ConcurrentHashMap::class.java,
            HashMap::class.java,
            HashSet::class.java,
            ConcurrentLinkedDeque::class.java,
            Component::class.java,
            Array<Component>::class.java,
            Class::class.java,
            Array<Any>::class.java,
            Map::class.java,
            Optional::class.java,
            Any::class.java,
            Network::class.java
        )
    }

    private fun registerAll(vararg classes: Class<*>) {
        topId = 40
        for (clazz in classes) {
            register(topId++, clazz)
        }
    }
}
