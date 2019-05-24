package com.gofficer.codenames.systems.server

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.gofficer.codenames.Network
import com.gofficer.codenames.components.*
import com.gofficer.codenames.screens.menu.SomeRequest
import com.gofficer.codenames.systems.Mappers
import ktx.ashley.allOf
import ktx.ashley.remove
import ktx.log.info
import javax.naming.Name


class ServerNetworkEntitySystem(
    val server: Server
) : IteratingSystem(allOf(NetworkComponent::class).get()) {

    private val animationDuration = 0.45f

    init {
        server.addListener(object : Listener() {
            override fun received(connection: Connection, someObject: Any) {
                info { "Connection: $connection"}
                when (someObject) {
                    is SomeRequest -> info { "Got someRequest: ${someObject.text}"}
                    is Network.Server.CardPressed -> {
                        info { "Got a card pressed action from client, broadcasting to all ${someObject.id}"}
                        // Send to all but original sender
                        server.sendToAllExceptTCP(connection.id, Network.Server.CardPressed(someObject.id))
                        // Or send to all
//                        server.sendToAllTCP(Network.Server.CardPressed(someObject.id))
                    }
                    is Network.Client.RequestCardSetup -> {

                        val names = engine.getEntitiesFor(allOf(NameComponent::class).get())
                        val cards = names.map {
                            val name = Mappers.name[it].name
                            val position = Mappers.transform[it].position
                            val isRevealed = Mappers.revealable[it].isRevealed
                            Network.Server.Card(name, position.x, position.y, isRevealed)
                        }
//                        val entities = getEntitiesFor(allOf(NameComponent::class))
                        server.sendToTCP(connection.id, Network.Server.Cards(cards))
                    }
                    else -> info { "Received unknown action"}
                }
            }
        })
    }

//    private val serverNetworkSystem by system<ServerNetworkSystem>()

    override fun processEntity(entity: Entity?, deltaTime: Float) {
//        serverNetworkSystem.sentEntity(entity)
        info { "Server Send $entity over the wire"}
        // TODO: Determine how I want to do this
        server.sendToAllTCP(Network.Server.CardPressed(1))
        entity?.remove<NetworkComponent>()
    }

}