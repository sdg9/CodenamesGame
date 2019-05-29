package com.gofficer.codenames.components

import com.artemis.Component
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.gofficer.codenames.utils.ExtendedComponent
import com.gofficer.codenames.utils.defaultCopyFrom

class TextureComponent() : Component(), ExtendedComponent<TextureComponent> {

    var texture: TextureRegion? = null

    override fun canCombineWith(other: TextureComponent) =
        this.texture == other.texture

    override fun copyFrom(other: TextureComponent) {
        this.defaultCopyFrom(other)
    }

}