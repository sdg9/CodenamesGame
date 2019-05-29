package com.gofficer.codenames.systems.server

import com.artemis.Aspect
import com.artemis.systems.IteratingSystem
import com.gofficer.codenames.GameServer


class ServerNetworkEntitySystem(
    val gameServer: GameServer
) : IteratingSystem(Aspect.all()) {
    override fun process(entityId: Int) {

        //TODO
        //for each player, check their list of entities spawned in their viewport,
        //compare with our list of entities that actually exist (spatial query)
    }

}