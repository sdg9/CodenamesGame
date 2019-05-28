package com.gofficer.codenames

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.signals.Signal
import com.badlogic.gdx.utils.SnapshotArray
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.EndPoint
import com.gofficer.codenames.components.*
import com.gofficer.codenames.screens.menu.SomeRequest
import com.gofficer.codenames.utils.registerClass

/**
 * A staple class of KryoNet examples, this class contains helper methods for registering classes for serialization as
 * well as defines framework-level message types.
 */
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
        kryo.registerClass<ArrayList<*>>()
        kryo.registerClass<SomeRequest>()
    }

    private fun registerComponents(kryo: Kryo) {
//        kryo.registerClass<Object>()
//        kryo.registerClass<SnapshotArray<*>>()
//        kryo.registerClass<Signal<*>>()
//        kryo.registerClass<Entity>()
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
        kryo.registerClass<NetworkComponent>()
    }

    private fun registerServer(kryo: Kryo) {
        kryo.registerClass<Server.CardPressed>()
        kryo.registerClass<Server.Card>()
        kryo.registerClass<Server.Cards>()
    }

    private fun registerClient(kryo: Kryo) {
        kryo.registerClass<Client.RequestCardSetup>()
    }

    object Server {
        class CardPressed(var id: Int = -1)

        class Card(var name: String = "", var x: Float = 0f, var y: Float = 0f, var isRevealed: Boolean = false)

        class Cards(var cards: List<Card> = listOf())
    }

    object Client {
        class RequestCardSetup

        class InitialClientData {

            /**
             * UUID of player associated with this name. used as a "password" of sorts.
             * so that another cannot log on with the same name and impersonate without that info.
             *
             *
             * Past this point, the server refers to players by id instead. Which is just session-persistent,
             * whereas UUID is world persistent.
             */
            var playerUUID: String? = null
            var playerName: String? = null

            var versionMajor: Int = 0
            var versionMinor: Int = 0
            var versionRevision: Int = 0
        }
    }

    object Shared {

        class DisconnectReason {
            lateinit var reason: Reason

            enum class Reason {
                VersionMismatch,
                InvalidPlayerName
            }
        }
    }
}


object NetworkHelper {
    fun debugPacketFrequencies(receivedObject: Any,
                               debugPacketFrequencyByType: MutableMap<String, Int>) {
        val debugPacketTypeName = receivedObject.javaClass.toString()
        val current = debugPacketFrequencyByType[debugPacketTypeName]

        if (current != null) {
            debugPacketFrequencyByType.put(debugPacketTypeName, current + 1)
        } else {
            debugPacketFrequencyByType.put(debugPacketTypeName, 1)
        }
    }
}