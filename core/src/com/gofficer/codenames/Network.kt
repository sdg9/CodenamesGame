package com.gofficer.codenames

import com.badlogic.ashley.core.Component
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.EndPoint
import com.gofficer.codenames.components.*
import com.gofficer.codenames.screens.menu.SomeRequest
import com.gofficer.codenames.utils.registerClass

object Network {
    const val PORT = 54553
    const val bufferObjectSize = 255032
    const val bufferWriteSize = 250536

    fun register(endPoint: EndPoint) {
        val kryo = endPoint.kryo

        registerClient(kryo)
        registerServer(kryo)
        registerShared(kryo)

        registerComponents(kryo)

    }

    private fun registerShared(kryo: Kryo) {
        kryo.registerClass<SomeRequest>()
    }

    private fun registerComponents(kryo: Kryo) {
        kryo.registerClass<Component>()
        kryo.registerClass<ClickableComponent>()
        kryo.registerClass<FlipAnimationComponent>()
        kryo.registerClass<NameComponent>()
        kryo.registerClass<RectangleComponent>()
        kryo.registerClass<RevealableComponent>()
        kryo.registerClass<StateComponent>()
        kryo.registerClass<TeamComponent>()
        kryo.registerClass<TextureComponent>()
        kryo.registerClass<TransformComponent>()
    }

    private fun registerServer(kryo: Kryo) {

    }

    private fun registerClient(kryo: Kryo) {

    }
}