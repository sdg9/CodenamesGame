package com.gofficer.codenames.systems.client

import com.badlogic.gdx.Gdx
import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.Network
import com.gofficer.codenames.network.client.ClientResponseProcessor
import com.gofficer.codenames.network.client.GameNotificationProcessor
import com.gofficer.codenames.network.interfaces.INotification
import com.gofficer.codenames.network.interfaces.INotificationProcessor
import com.gofficer.codenames.network.interfaces.IResponse
import com.gofficer.codenames.network.interfaces.IResponseProcessor
import net.mostlyoriginal.api.network.marshal.kryonet.KryonetClientMarshalStrategy
import net.mostlyoriginal.api.network.system.MarshalSystem

class ClientNetworkSystem(host: String, port: Int) :
    MarshalSystem(Network.NetworkDictionary(), KryonetClientMarshalStrategy(host, port)) {
//    MarshalSystem(NetworkDictionary(), KryonetClientMarshalStrategy(host, port)) {

    val kryonetClient: KryonetClientMarshalStrategy
        get() = marshal as KryonetClientMarshalStrategy



    override fun received(connectionId: Int, obj: Any?) {
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

        var responseProcessor: IResponseProcessor = ClientResponseProcessor()
        var notificationProcessor: INotificationProcessor = GameNotificationProcessor()
    }
}
