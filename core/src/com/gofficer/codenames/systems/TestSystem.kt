package com.gofficer.codenames.systems

import com.artemis.BaseSystem
import com.artemis.annotations.Wire
import ktx.log.logger

@Wire
class TestSystem : BaseSystem() {


    companion object {
        val log = logger<TestSystem>()
    }

    override fun processSystem() {
//        log.debug { "Test tick" }
    }

}