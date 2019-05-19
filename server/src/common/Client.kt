package com.gofficer.colyseus.server

import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.codenames.redux.actions.ClientOptions
import com.gofficer.colyseus.network.ProtocolMessage
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory
import kotlin.reflect.jvm.jvmName

var moshiPack = MoshiPack()


private val logger by lazy { LoggerFactory.getLogger(Client::class.jvmName) }

data class Client(
    var socket: WebSocketSession,
    var id: String,
    var sessionId: String?,
    var options: ClientOptions?,
    var auth: Any?,
    var pingCount: Int?,
    val useTextOverBinary: Boolean = false
//    var listener: ClientListner? = null
) {
    var onMessageListener = mutableListOf<(DecryptProtocol) -> Unit>()
    var onCloseListener = mutableListOf<(code: Int) -> Unit>()

    fun setOnMessageListener(handler: (DecryptProtocol) -> Unit ) {
        onMessageListener.add(handler)
    }
    fun setOnCloseListener(handler: (code: Int) -> Unit ) {
        onCloseListener.add(handler)
    }
    fun removeAllListeners() {
        onMessageListener = mutableListOf<(DecryptProtocol) -> Unit>()
        onCloseListener = mutableListOf<(code: Int) -> Unit>()
    }


    suspend fun sendUserId(id: String) {
        logger.debug("Sending USER_ID")
        val byteArray = moshiPack.packToByteArray(listOf(
            Protocol.USER_ID,
            id
        ))
        socket.send(byteArray)

//        val packer = MessagePack.newDefaultBufferPacker()
//        packer.packArrayHeader(2)
//            .packInt(Protocol.USER_ID)
//            .packString(id)
//            .close()
//
//
//        socket.send(packer.toByteArray())
//        val packer = PackerConfig()
//            .withSmallStringOptimizationThreshold(256) // String
//            .newBufferPacker()
//        packer.packString(id)
//        packer.close()
//        val binaryMessage = packProtocol(Protocol.USER_ID, packer.toByteArray())
//        val packer = PackerConfig()
//            .withSmallStringOptimizationThreshold(256) // String
//            .newBufferPacker()
//        println("Sending binary message to client: $binaryMessage")
//        socket.send(binaryMessage)
    }

    suspend fun sendJoinRoom(roomId: String, processId: String?) {
        logger.debug("Sending JOIN_ROOM")
        val byteArray = moshiPack.packToByteArray(listOf(
            Protocol.JOIN_ROOM,
            roomId,
            1
        ))
        socket.send(byteArray)
//        val packer = MessagePack.newDefaultBufferPacker()
//        packer.packArrayHeader(3)
//            .packInt(Protocol.JOIN_ROOM)
//            .packString(roomId)
//            .packInt(1) // TODO watching colyseus they send a value here, so far I only see 1 (default if local process?), this fails if a string
////            .packString(processId ?: "1")
//            .close()

//        socket.send(packer.toByteArray())
    }

    suspend fun <T> sendRoomState(state: T) {
        logger.debug("Sending ROOM_STATE")
        val byteArray = moshiPack.packToByteArray(listOf(
            Protocol.ROOM_STATE,
            state
        ))
        socket.send(byteArray)
    }


    suspend fun <T> sendRoomData(data: T) {
        logger.debug("Sending ROOM_STATE")
        val byteArray = moshiPack.packToByteArray(listOf(
            Protocol.ROOM_DATA,
            data
        ))
        socket.send(byteArray)
    }

    suspend fun sendJoinConfirmation(roomId: String) {
        logger.debug("Sending JOIN_CONFIRMATION")
        val byteArray = moshiPack.packToByteArray(listOf(
            Protocol.JOIN_ROOM,
            roomId
        ))
        socket.send(byteArray)
    }
//
//    suspend fun send(action: Action) {
//        val json = toJSON(action)
//        if (useTextOverBinary) {
//            socket.send(json)
//        } else {
//            val data = json.toByteArray()
//            socket.send(Frame.Binary(true, ByteBuffer.wrap(data)))
//        }
//    }
//
//    suspend fun send(message: String) {
//        if (useTextOverBinary) {
//            socket.send(message)
//        } else {
//            val data = message.toByteArray()
//            socket.send(Frame.Binary(true, ByteBuffer.wrap(data)))
//        }
//    }
}
//
//suspend inline fun <reified T> Client.sendUserId(id: String) {
//    val msgpack = MessagePack()
//    val raw = msgpack.write(id)
//    packProtocol(Protocol.USER_ID, id)
//    socket.send(Frame.Binary())
//}

suspend inline fun <reified T> Client.send(input: T) {
    println("Temporarily disabling generic send $input")
    return
//    if (useTextOverBinary) {
//        when (input) {
//            is Action -> socket.send(toJSON(input))
//            is String -> socket.send(input)
//            else -> throw Error("Unable to understand message format for $input")
//        }
//    } else {
//        when (input) {
//            is Action -> socket.send(Frame.Binary(true, ByteBuffer.wrap(toJSON(input).toByteArray())))
//            is String -> socket.send(Frame.Binary(true, ByteBuffer.wrap(input.toByteArray())))
//            else -> throw Error("Unable to understand message format for $input")
//        }
//    }
}


//interface ClientListner {
//    fun message(message: String)
//    fun close(code: Int?)
//}