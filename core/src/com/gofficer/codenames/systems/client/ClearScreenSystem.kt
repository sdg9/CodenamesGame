package com.gofficer.codenames.systems.client

import com.artemis.BaseSystem
import com.gofficer.codenames.utils.clearScreen

class ClearScreenSystem : BaseSystem() {
    override fun processSystem() {
        clearScreen()
    }

}