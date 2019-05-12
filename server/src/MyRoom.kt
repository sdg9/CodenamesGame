package com.example

import com.example.common.Client
import com.example.common.presence.LocalPresence
import com.example.common.sendAction
import common.Room
import common.RoomListener
import org.slf4j.LoggerFactory
import kotlin.reflect.jvm.jvmName


data class MyRoomGameState(
    val someText: String,
    val someNumber: Int,
    val someBoolean: Boolean
)

private val logger by lazy { LoggerFactory.getLogger(MyRoom::class.jvmName) }
class MyRoom : Room<MyRoomGameState>(listener = object : RoomListener {
    override fun create(room: Room<*>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dispose(room: Room<*>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun join(room: Room<*>, client: Client) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun leave(room: Room<*>, client: Client) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}) {
    override suspend fun onMessage(client: Client, data: Any) {
        logger.debug("My custom implementation received $data")

        clients.forEach {
            it.socket.sendAction(data)
        }

    }

    override fun onJoin(client: Client, options: Any?, auth: Any?) {
        logger.debug("Client ${client.id} joined room $roomId")
    }

    override fun onLeave(client: Client, consented: Boolean?) {
        logger.debug("Client ${client.id} left room $roomId")
    }

    override val state: MyRoomGameState = MyRoomGameState("test", 12, false)
}