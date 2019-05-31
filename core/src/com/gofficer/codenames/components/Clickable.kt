package com.gofficer.codenames.components

import com.artemis.Component

class Clickable : Component() {

    var state = ClickState.NONE

    enum class ClickState {
        NONE,
        HOVER,
        CLICKED
    }
}