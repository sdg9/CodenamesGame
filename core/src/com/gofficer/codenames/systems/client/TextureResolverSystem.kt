package com.gofficer.codenames.systems.client

import com.artemis.BaseEntitySystem
import com.artemis.annotations.All
import com.artemis.annotations.Exclude
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.components.TextureReferenceComponent
import com.gofficer.codenames.components.TextureRenderableComponent
import com.gofficer.codenames.utils.mapper

@All(TextureReferenceComponent::class)
@Exclude(TextureRenderableComponent::class)
class TextureResolverSystem(private val gameWorld: GameWorld) : BaseEntitySystem() {


    private val mTextureRenderable by mapper<TextureRenderableComponent>()

    override fun processSystem() {
    }

    override fun inserted(id: Int) {
        // once inserted, aspect.exclude(SpineRenderable.class) is longer satisfied.
        // removing the SpineRenderable would immediately recreate it; can be useful
        // when dealing with reloaded textures, editor tooling etc.
        assignTexture(id)
    }

    private fun assignTexture(id: Int) {
//        val cTextureRenderable = mTextureRenderable.create(id)

        // TODO for now always give same texture, in future drive off value of texture reference
        mTextureRenderable.create(id).apply {
            textureRegion = gameWorld?.client?.cardTexture
        }
//        cTextureRenderable.textureRegion = gameWorld?.client?.cardTexture

    }

}