package com.gofficer.codenames.systems.server

import com.artemis.BaseSystem
import com.gofficer.codenames.GameServer

abstract class DefaultManager(val server: GameServer) : BaseSystem() {

    override fun processSystem() {}
}
