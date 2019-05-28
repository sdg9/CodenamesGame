package com.gofficer.codenames.systems.server

import com.artemis.Aspect
import com.artemis.systems.IntervalIteratingSystem

/**
 * Another Artemis system class is responsible for game state outbound packets that processes a constant 20 times per
 * second. Any state changes are put into a queue and flushed each time this system processes. It is also responsible
 * for additional packet flow control on-top of what TCP offers.
 */
class ServerUpdateSystem(): IntervalIteratingSystem(Aspect.all(), 50f) {
    override fun process(entityId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}