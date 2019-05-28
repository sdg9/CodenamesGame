package com.gofficer.codenames.systems.client

import com.artemis.Aspect
import com.artemis.systems.IntervalIteratingSystem

/**
 * The client-side version of ServerUpdateSystem, this queues commands to send to the server. Its interval is 33 packets
 * per second.
 *
// https://medium.com/@rizza/winter-games-part-5-5151f458cbea
 */
class ClientUpdateSystem(): IntervalIteratingSystem(Aspect.all(), 50f) {
    override fun process(entityId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}