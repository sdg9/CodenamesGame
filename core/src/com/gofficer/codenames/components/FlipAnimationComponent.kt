package com.gofficer.codenames.components

import com.artemis.Component

class FlipAnimationComponent(var hasStarted: Boolean = false, var initialX: Float = 0f, var initialWidth: Float = 0f, var time: Float = 0f, var suppressColor: Boolean = true) :
    Component()
