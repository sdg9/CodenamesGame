//package com.gofficer.colyseus.server
//
//import java.io.ByteArrayOutputStream
//import java.util.*
//
//class Protocol {
//
//    companion object {
//
//        val WS_CLOSE_CONSENTED = 4000;
//
//
//        // User-related (1~8)
//        val USER_ID = 1
//
//        // Room-related (9~19)
//        val JOIN_REQUEST = 9
//        val JOIN_ROOM = 10
//        val JOIN_ERROR = 11
//        val LEAVE_ROOM = 12
//        val ROOM_DATA = 13
//        val ROOM_STATE = 14
//        val ROOM_STATE_PATCH = 15
//
//        // Match-making related (20~29)
//        val ROOM_LIST = 20
//
//        // Generic messages (50~60)
//        val BAD_REQUEST = 50
//
//        // WebSocket error codes
//        val WS_SERVER_DISCONNECT = 4201
//        val WS_TOO_MANY_CLIENTS = 4202
//    }
//}
//
//data class Message(val protocol: Int, val message: String)
//
//data class MessagePackWebsitePlug(var compact: Boolean = true, var schema: Int = 0)
//
//// TODO: Currently tehse are duplciated in clietn as well, move to shared module between client & server
//fun packProtocol(protocol: Int, bytes: ByteArray): ByteArray {
//    val outputStream = ByteArrayOutputStream()
//    outputStream.write(protocol)
//    outputStream.write(bytes)
//    return outputStream.toByteArray()
//}
//
//data class DecryptProtocol(val protocol: Int, val byteArray: ByteArray)
//fun unpackProtocol(bytes: ByteArray): DecryptProtocol {
//    val protocol = bytes[0].toInt()
//    val newByteArray = Arrays.copyOfRange(bytes, 1, bytes.size)
//    return DecryptProtocol(protocol, newByteArray)
//}