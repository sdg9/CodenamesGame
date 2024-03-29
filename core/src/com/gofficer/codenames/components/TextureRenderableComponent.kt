package com.gofficer.codenames.components

import com.artemis.Component
import com.artemis.annotations.Transient
import com.badlogic.gdx.graphics.g2d.TextureRegion

@Transient
class TextureRenderableComponent : Component() {
    var textureRegion: TextureRegion? = null
}