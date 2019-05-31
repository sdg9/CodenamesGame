package com.gofficer.codenames.systems.client

import com.badlogic.gdx.Gdx
import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.GameWorld
import com.gofficer.codenames.NetworkDictionary
import com.gofficer.codenames.network.client.ClientResponseProcessor
import com.gofficer.codenames.network.client.GameNotificationProcessor
import com.gofficer.codenames.network.interfaces.INotification
import com.gofficer.codenames.network.interfaces.INotificationProcessor
import com.gofficer.codenames.network.interfaces.IResponse
import com.gofficer.codenames.network.interfaces.IResponseProcessor
import ktx.log.logger
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetClientMarshalStrategy
import net.mostlyoriginal.api.network.system.MarshalSystem

class clientNetworkSystem(gameWorld: GameWorld, host: String, port: Int) :
    MarshalSystem(NetworkDictionary(), KryonetClientMarshalStrategy(host, port)) {
//    MarshalSystem(NetworkDictionary(), KryonetClientMarshalStrategy(host, port)) {

    var responseProcessor: IResponseProcessor = ClientResponseProcessor(gameWorld)
    var notificationProcessor: INotificationProcessor = GameNotificationProcessor(gameWorld)

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
        val log = logger<clientNetworkSystem>()

    }
}
