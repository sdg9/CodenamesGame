package com.gofficer.codenames.systems.client

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.gofficer.codenames.Network
import com.gofficer.codenames.components.*
import com.gofficer.codenames.screens.menu.SomeRequest
import com.gofficer.codenames.systems.Mappers
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.log.info


class ClientNetworkEntitySystem(val client: Client) : IteratingSystem(allOf(NetworkComponent::class).get()) {

    private val animationDuration = 0.45f

    init {
        client.addListener(object : Listener() {
            override fun received(connection: Connection, someObject: Any) {
                info { "Client Connection: $connection"}
                when (someObject) {
                    is SomeRequest -> info { "Client Got someRequest: ${someObject.text}"}
                    is Network.Server.CardPressed -> info { "Client Got a card pressed action ${someObject.id}"}
                    is Network.Server.Cards -> {
//                        someObject.cards
                        info { "Got cards ${someObject.cards}"}
                        // TODO setup board
                        // Ideally: dispatch(SetupBoard, cards)
                    }
                    else -> info { "Client Received unknown action"}
                }
            }
        })
    }

//    private val serverNetworkSystem by system<ServerNetworkSystem>()

    override fun processEntity(entity: Entity?, deltaTime: Float) {
//        serverNetworkSystem.sentEntity(entity)
        info { "Client Send $entity over the wire"}
        client.sendTCP(Network.Server.CardPressed(1))
        entity?.remove<NetworkComponent>()
    }

}