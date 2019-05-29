package com.gofficer.codenames

import com.artemis.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.SnapshotArray
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import com.esotericsoftware.kryonet.EndPoint
import com.gofficer.codenames.components.*
import com.gofficer.codenames.screens.menu.SomeRequest
import com.gofficer.codenames.utils.registerClass
import java.util.*
import kotlin.collections.ArrayList

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
        kryo.registerClass<Color>()

        kryo.registerClass<Shared.DisconnectReason>()
        kryo.registerClass<Shared.DisconnectReason.Reason>()
        //modular components. some components are too fucking huge and stupid to serialize automatically (like Sprite),
        //so we split up only what we need.

        // primitives/builtin
        kryo.registerClass<ByteArray>()
        kryo.registerClass<IntArray>()

//        kryo.register(ArrayList::class.java)
        kryo.registerClass<ArrayList<Any>>()

        kryo.registerClass<kotlin.Array<Any>>()
        kryo.registerClass<kotlin.Array<Any>>()
        kryo.registerClass<Vector2>()
        kryo.registerClass<IntArray>()
        kryo.registerClass<Rectangle>()
        kryo.register(EnumSet::class.java, DefaultSerializers.EnumSetSerializer())
    }

    private fun registerComponents(kryo: Kryo) {
//        kryo.registerClass<Object>()
//        kryo.registerClass<SnapshotArray<*>>()
//        kryo.registerClass<Signal<*>>()
//        kryo.registerClass<Entity>()
        kryo.registerClass<Component>()
//        kryo.registerClass<ClickableComponent>()
//        kryo.registerClass<FlipAnimationComponent>()
//        kryo.registerClass<NameComponent>()
//        kryo.registerClass<RectangleComponent>()
//        kryo.registerClass<RevealableComponent>()
//        kryo.registerClass<StateComponent>()
//        kryo.registerClass<TeamComponent>()
        kryo.registerClass<CardComponent>()
        kryo.registerClass<TextureComponent>()
        kryo.registerClass<TransformComponent>()
//        kryo.registerClass<NetworkComponent>()
    }

    private fun registerServer(kryo: Kryo) {
        kryo.registerClass<Server.EntityDestroyMultiple>()
        kryo.registerClass<Server.EntitySpawnMultiple>()
        kryo.registerClass<Server.CardPressed>()
        kryo.registerClass<Server.Card>()
        kryo.registerClass<Server.Cards>()
        kryo.registerClass<Server.PlayerSpawned>()

        kryo.registerClass<Server.SpawnCards>()
        kryo.registerClass<Server.EntitySpawn>()
    }

    private fun registerClient(kryo: Kryo) {
        kryo.registerClass<Client.RequestCardSetup>()
        kryo.registerClass<Client.InitialClientData>()
    }

    object Server {
        class CardPressed(var id: Int = -1)

        class Card(var name: String = "", var x: Float = 0f, var y: Float = 0f, var isRevealed: Boolean = false)

        class Cards(var cards: List<Card> = listOf())

        class PlayerSpawned(
            // session local id, to be displayed
            var connectionId: Int = 0,
            var playerName: String = "",
            var pos: Vector2 = Vector2()
            //we don't need a size packet for player. we know how big one will be, always.
        )

        /**
         * Sends the client a list of cards to spawn
         */
        class SpawnCards {
            var entitiesToSpawn = mutableListOf<EntitySpawn>()
        }

        class EntitySpawn {
            var size: Vector2 = Vector2()
            var pos: Vector2 = Vector2()

            var textureName: String = ""

            var id: Int = 0

            var components = listOf<Component>()
        }

        class EntitySpawnMultiple {
            var entitySpawn = mutableListOf<EntitySpawn>()
        }
        /**
         * Tells client to destroy certain entities that it shouldn't have
         * spawned anymore (outside of players region). The entities
         * probably still exist in the world on the server, as well
         * as possibly on other clients.
         *
         * @see EntityKilled
         */
        class EntityDestroyMultiple {
            lateinit var entitiesToDestroy: List<Int>
        }
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