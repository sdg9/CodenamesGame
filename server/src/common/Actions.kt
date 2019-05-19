package com.gofficer.colyseus.server

import com.gofficer.codenames.redux.actions.ClientOptions
import com.squareup.moshi.Json
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import gofficer.codenames.redux.game.GameState
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.http.cio.websocket.send
import okio.Buffer
import java.lang.Exception
import kotlin.system.measureTimeMillis

//
//class Actions {
//
//    companion object {
//        // User-related (1~8)
//        val USER_CONNECTED = "USER_CONNECTED"
//    }
//}

/* Protocol concepts
socket message types
====================
  // User-related (1~8)
  USER_ID = 1,

  // Room-related (9~19)
  JOIN_REQUEST = 9,
  JOIN_ROOM = 10,
  JOIN_ERROR = 11,
  LEAVE_ROOM = 12,
  ROOM_DATA = 13,
  ROOM_STATE = 14,
  ROOM_STATE_PATCH = 15,

  // Match-making related (20~29)
  ROOM_LIST = 20,

  // Generic messages (50~60)
  BAD_REQUEST = 50,

  // WebSocket error codes
  WS_SERVER_DISCONNECT = 4201,
  WS_TOO_MANY_CLIENTS = 4202,

room action types
=================
  i.e. redux com.gofficer.codenames.redux.actions

*/
//
//enum class ActionType {
//    JOIN_REQUEST,
//    JOIN_RESPONSE,
//    JOIN_RESPONSE_CONFIRMATION,
//
//
//    JOIN_ERROR,
//    USER_ID,
//    USER_CONNECTED,
//    SOMETHING_ELSE,
//    ROOM_LIST,
//    PARSE_ERROR,
//
//    SET_STATE
//}
//
//sealed class Action(val type: ActionType)
//
//data class SetState(val state: GameState): Action(ActionType.SET_STATE)
////data class UserHandshakeStep1()
//data class JoinError(val message: String): Action(ActionType.JOIN_ERROR)
//data class JoinRequest(val room: String, val joinOptions: ClientOptions?): Action(ActionType.JOIN_REQUEST)
//data class JoinResponse(val requestId: Int?, val roomId: String, val processId: String?): Action(ActionType.JOIN_RESPONSE)
//data class JoinResponseConfirmation(val roomId: String): Action(ActionType.JOIN_RESPONSE_CONFIRMATION)
//data class UserId(val id: String, val pingCount: Int) :Action(ActionType.USER_ID)
//data class UserConnected(val id: String, val name : String ) : Action(ActionType.USER_CONNECTED)
//data class SomethingElse(val name : String ) : Action(ActionType.SOMETHING_ELSE)
//
//
////fun getAdapterFromActionType(type: ActionType?): Class? {
////    return when (type) {
////        ActionType.USER_CONNECTED -> UserConnected
////        ActionType.SOMETHING_ELSE -> SomethingElse
////        else -> null
////    }
////}
//
//fun getActionTypeFromJson(json: String): ActionType? {
//    val reader = JsonReader.of(Buffer().writeUtf8(json))
//    try {
//        val value = reader.readJsonValue() as Map<String, Object>
//        val type = value.get("type") as String
//        return ActionType.valueOf(type)
//    } catch (e: Exception) {
//        println("Error $e")
//    }
//    return null
//}
//
//fun parseActionJSON(json: String): Action? {
//    val type = getActionTypeFromJson(json) ?: return null
//    return when (type) {
//        ActionType.SET_STATE -> fromJson<SetState>(json)
//        ActionType.USER_ID -> fromJson<UserId>(json)
//        ActionType.USER_CONNECTED -> fromJson<UserConnected>(json)
////        else -> null
//        ActionType.SOMETHING_ELSE -> fromJson<SomethingElse>(json)
//        ActionType.PARSE_ERROR -> TODO()
//        ActionType.JOIN_REQUEST -> fromJson<JoinRequest>(json)
//        ActionType.ROOM_LIST -> TODO()
//        ActionType.JOIN_ERROR -> TODO()
//        ActionType.JOIN_RESPONSE -> TODO()
//        ActionType.JOIN_RESPONSE_CONFIRMATION -> TODO()
//    }
//}
//
//fun getMoshiBuilder(): Moshi {
//    var builder: Moshi? = null
//
//    val time = measureTimeMillis {
//        builder = Moshi.Builder()
//            .add(
//                PolymorphicJsonAdapterFactory.of(Action::class.java, "action")
//                    .withSubtype(UserConnected::class.java, ActionType.USER_CONNECTED.name)
//                    .withSubtype(UserId::class.java, ActionType.USER_ID.name)
//                    .withSubtype(SomethingElse::class.java, ActionType.SOMETHING_ELSE.name)
//
//            )
////        .add(KotlinJsonAdapterFactory())
//            .build()
//    }
//    println("Builder took $time")
//
//    return builder!!
//}
//
//
//fun getAttributeFromActionJson(json: String, attribute: String): Any? {
//    val reader = JsonReader.of(Buffer().writeUtf8(json))
//    try {
//        val value = reader.readJsonValue() as Map<String, Object>
//        return value.get(attribute)
//    } catch (e: Exception) {
//        println("Error $e")
//    }
//    return null
//}
//
//inline fun <reified T> toJSON(message: T): String {
//    val jsonAdapter = getMoshiBuilder().adapter(T::class.java)
//    return jsonAdapter.toJson(message)
//}
//
//private inline fun <reified T> fromJson(json: String): T? {
//    val jsonAdapter = getMoshiBuilder().adapter(T::class.java)
//    return jsonAdapter.fromJson(json)
//}

//suspend inline fun <reified T> WebSocketSession.sendAction(action: T) {
//    TODO("Not currently implemented")
////    val jsonAdapter = getMoshiBuilder().adapter(T::class.java)
////    val json = jsonAdapter.toJson(action)
////    send(json)
////    send(toJSON(action))
//}