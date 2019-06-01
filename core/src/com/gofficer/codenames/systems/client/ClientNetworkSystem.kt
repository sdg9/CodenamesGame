package com.gofficer.codenames.systems.client

import com.badlogic.gdx.Gdx
import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.Network
import com.gofficer.codenames.network.client.ClientResponseProcessor
import com.gofficer.codenames.network.client.ClientNotificationProcessor
import com.gofficer.codenames.network.interfaces.INotification
import com.gofficer.codenames.network.interfaces.INotificationProcessor
import com.gofficer.codenames.network.interfaces.IResponse
import com.gofficer.codenames.network.interfaces.IResponseProcessor
import ktx.log.logger
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetClientMarshalStrategy
import net.mostlyoriginal.api.network.system.MarshalSystem

class ClientNetworkSystem(gameWorld: GameWorld, host: String, port: Int) :
    MarshalSystem(Network.NetworkDictionary(), KryonetClientMarshalStrategy(host, port)) {
//    MarshalSystem(NetworkDictionaryHelper(), KryonetClientMarshalStrategy(host, port)) {

    var responseProcessor: IResponseProcessor = ClientResponseProcessor(gameWorld)
    var notificationProcessor: INotificationProcessor = ClientNotificationProcessor(gameWorld)

    val kryonetClient: KryonetClientMarshalStrategy
        get() = marshal as KryonetClientMarshalStrategy


    override fun connected(connectionId: Int) {
        super.connected(connectionId)
        log.debug { "Client $connectionId connected"}
//        clientSystem.getKryonetClient().sendToAll(JoinRoomRequest(selected.getId()))
    }

    override fun received(connectionId: Int, obj: Any?) {
        log.debug { "Received a message from server: $obj"}
        Gdx.app.postRunnable {
            Log.info(obj.toString())
            when (obj) {
                is IResponse -> obj.accept(responseProcessor)
                is INotification -> obj.accept(notificationProcessor)
            }
        }
//        super.received(connectionId, `object`)
    }

    companion object {
        val log = logger<ClientNetworkSystem>()

    }
}
