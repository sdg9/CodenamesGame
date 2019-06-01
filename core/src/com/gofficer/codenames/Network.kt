package com.gofficer.codenames

import com.artemis.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import com.esotericsoftware.kryonet.EndPoint
import com.gofficer.codenames.components.*
import com.gofficer.codenames.network.interfaces.IRequest
import com.gofficer.codenames.network.interfaces.IRequestProcessor
import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.utils.registerClass
import net.mostlyoriginal.api.network.marshal.common.MarshalDictionary
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.collections.ArrayList

/**
 * A staple class of KryoNet examples, this class contains helper methods for registering classes for serialization as
 * well as defines framework-level message types.
 */
object Network {

    const val INVALID_ENTITY_ID = -1
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
    class NetworkDictionary : MarshalDictionary() {

        init {

            registerAll(
                // Game Requests
                Array<Any>::class.java,
                // Other
                Array<BooleanArray>::class.java,
                BooleanArray::class.java,
                Array<IntArray>::class.java,
                IntArray::class.java,
                Array<Int>::class.java,
                ConcurrentHashMap::class.java,
                HashMap::class.java,
                HashSet::class.java,
                ConcurrentLinkedDeque::class.java,
                Component::class.java,
                Array<Component>::class.java,
                Class::class.java,
//                KlassArray::class.java,
//                Array<KlassArray>
//                Array<Class<Any>>::class.java,
//                java.lang.Class[]::class.java,
//                Array<Class<Any>>::class.java,
//                Array<Class<*>>::class.java,
                Array<String>::class.java,
                Array<Any>::class.java,
                Map::class.java,
                Optional::class.java,
                Any::class.java,
                Network::class.java,

                Color ::class.java,
                ByteArray::class.java,
                IntArray::class.java,
                ArrayList::class.java,

                Vector2::class.java,
                IntArray::class.java,
                Rectangle::class.java,

                com.badlogic.gdx.utils.Array::class.java,


                Shared.DisconnectReason::class.java,
                Color::class.java,
                Shared.DisconnectReason::class.java,
                Shared.DisconnectReason.Reason::class.java,

                EntityUpdate::class.java,
//                EntityUpdate.EntityUpdateBuilder::class.java,
                Shared.Player::class.java,

                // Components
                Component::class.java,
                CardComponent::class.java,
                TextureComponent::class.java,
                PositionComponent::class.java,
                TextureReferenceComponent::class.java,
                RevealedComponent::class.java,
                NetworkComponent::class.java,
                TransformComponent::class.java,


                Server.EntitySpawnMultiple::class.java,
                Server.CardPressed::class.java,

                Client.JoinRoomRequest::class.java
            )
        }

        private fun registerAll(vararg classes: Class<*>) {
            topId = 40
            for (clazz in classes) {
                register(topId++, clazz)
            }
//            register(topId++, Array<Class<*>>::class.java)
            register(topId++, NetworkDictionaryHelper.getClazz())
        }
    }
//    class NetworkDictionaryHelper : MarshalDictionary(
//        Color::class.java,
//        ByteArray::class.java,
//        IntArray::class.java,
//        ArrayList::class.java,
//
//        Vector2::class.java,
//        IntArray::class.java,
//        Rectangle::class.java,
//
//
//
//        Shared.DisconnectReason::class.java,
//        Color::class.java,
//        Shared.DisconnectReason::class.java,
//        Shared.DisconnectReason.Reason::class.java,
//
//        EntityUpdate::class.java,
//        Shared.Player::class.java,
//
//        // Components
//        Component::class.java,
//        CardComponent::class.java,
//        TextureComponent::class.java,
//        PositionComponent::class.java,
//        TextureReferenceComponent::class.java,
//        RevealedComponent::class.java,
//        Server.EntitySpawnMultiple::class.java,
//        Server.CardPressed::class.java
//
//
//    ) {
//
//    }

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

        kryo.registerClass<EntityUpdate>()
        kryo.registerClass<Shared.Player>()
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
        kryo.registerClass<PositionComponent>()
        kryo.registerClass<TextureReferenceComponent>()
        kryo.registerClass<RevealedComponent>()
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
        kryo.registerClass<Server.CardTouched>()
    }

    private fun registerClient(kryo: Kryo) {
        kryo.registerClass<Client.RequestCardSetup>()
        kryo.registerClass<Client.InitialClientData>()
        kryo.registerClass<Client.EntityTouch>()
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

        class CardTouched(var entityId: Int = INVALID_ENTITY_ID)

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

        /**
         * request for the entity to be touched
         * (Card flip)
         */
        class EntityTouch(var entityId: Int = 0)


        class JoinRoomRequest: IRequest {
            override fun accept(processor: IRequestProcessor, connectionId: Int) {
                processor.processRequest(this, connectionId)
            }

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

        class Player {

            var connectionId: Int = -1
            var playerName: String = ""
            var isReady: Boolean = false

            constructor() {}

            @JvmOverloads
            constructor(connectionId: Int, playerName: String) {
                this.connectionId = connectionId
                this.playerName = playerName
            }

            override fun toString(): String {
                return "$playerName Ready: $isReady"
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