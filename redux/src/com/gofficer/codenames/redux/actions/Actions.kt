package com.gofficer.codenames.redux.actions


import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.codenames.redux.models.Card
import com.gofficer.colyseus.network.*
//import com.squareup.moshi.JsonReader
import gofficer.codenames.redux.game.GameState
import org.apache.commons.codec.binary.Hex

/**
 * Generic Actions:
 *
 *
 * Gameplay Actions:
 * SetupGame
 * CardPressed
 *
 */

interface BaseAction

interface LocalAction : BaseAction

data class ChangeScene(val screenName: String) : LocalAction

enum class ActionType {
    JOIN_REQUEST,
    JOIN_RESPONSE,
    JOIN_RESPONSE_CONFIRMATION,


    JOIN_ERROR,
    USER_ID,
    USER_CONNECTED,
    SOMETHING_ELSE,
    ROOM_LIST,
    PARSE_ERROR,

    SET_STATE,

    SETUP_GAME,

    RESET_GAME,

    SETUP_CARDS,

    CARD_PRESSED
}

// TODO move somewhere logical (server and client need to be in sync)
data class ClientOptions(
    var auth: String?,
    var requestId: Int?,
    var sessionId: String?
)

sealed class Action(val type: ActionType): BaseAction

sealed class Type(val type: SubProtocol): BaseAction

sealed class NetworkAction: BaseAction {
    var isFromServer: Boolean = false
}

sealed class NetworkProtocolAction(val type: Int) : BaseAction {
    var isFromServer: Boolean = false
}


// GamePlay Action

//data class TouchCard(val id: Int): NetworkProtocolAction(SubProtocol.TOUCH_CARD)

data class TouchCard(val id: Int): NetworkAction()

//data class CardPressed(val id: Int, val word: String, override var isFromServer: Boolean = false): Action(ActionType.CARD_PRESSED), NetworkAction


//
data class SetupCards(val cards: List<Card>) : Action(ActionType.SETUP_CARDS)
//
class ResetGame : Action(ActionType.RESET_GAME)
//
class SetupGame : Action(ActionType.SETUP_GAME)


data class SetState(val state: GameState): BaseAction
//data class SetState(val state: GameState): Action(ActionType.SET_STATE)
//data class UserHandshakeStep1()
data class JoinError(val message: String): Action(ActionType.JOIN_ERROR)
data class JoinRequest(val room: String, val joinOptions: ClientOptions?): Action(ActionType.JOIN_REQUEST)
data class JoinResponse(val requestId: Int?, val roomId: String, val processId: String?): Action(ActionType.JOIN_RESPONSE)
data class JoinResponseConfirmation(val roomId: String): Action(ActionType.JOIN_RESPONSE_CONFIRMATION)
data class UserId(val id: String, val pingCount: Int) :Action(ActionType.USER_ID)
data class UserConnected(val id: String, val name : String ) : Action(ActionType.USER_CONNECTED)
data class SomethingElse(val name : String ) : Action(ActionType.SOMETHING_ELSE)




// Data Flow:
// Action -> ByteArray
// ByteArray -> Protocol Message -> Action

fun networkBytesToProtocol(byteArray: ByteArray?): ProtocolMessage? {
    if (byteArray == null) {
        return null
    }
    return unpackUnknown(byteArray)
}

fun protocolToAction(protocolMessage: ProtocolMessage?) : NetworkAction? {
        val subProtocol = protocolMessage?.subProtocol
        val message = protocolMessage?.message
        if(subProtocol == null || message == null) {
            return null
        }

        return when (subProtocol) {
            SubProtocol.TOUCH_CARD -> MoshiPack.unpack<TouchCard>(message)
            else -> null
        }
}

fun networkBytesToAction(byteArray: ByteArray?): NetworkAction? {
    return protocolToAction(networkBytesToProtocol(byteArray))
}

fun actionToNetworkBytes(action: NetworkAction) : ByteArray? {
    val protocol = Protocol.ROOM_DATA
    return when (action) {
        is TouchCard -> pack(protocol, SubProtocol.TOUCH_CARD, action)
        else -> null
    }
}

//
//fun actionToProtocol(action: NetworkAction) : ByteArray? {
//    val protocol = Protocol.ROOM_DATA
//    return when (action) {
//        is TouchCard -> pack(protocol, SubProtocol.TOUCH_CARD, action)
//        else -> null
//    }
//}

fun bytesToHexString(byteArray: ByteArray?): String {
    val pattern: Regex = "(\\S{2})".toRegex()
    return Hex.encodeHexString(byteArray).replace(pattern, "$1 ")
}











//fun getAdapterFromActionType(type: ActionType?): Class? {
//    return when (type) {
//        ActionType.USER_CONNECTED -> UserConnected
//        ActionType.SOMETHING_ELSE -> SomethingElse
//        else -> null
//    }
//}

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
//fun dispatchJsonAsOriginalAction(json: String, store: Store<*>) {
//    println("Test")
//    val action = parseActionJSON(json)
//    println("Dispatching action $action")
//    store.dispatch(action)
//}
//
//fun parseActionJSON(json: java.util.LinkedHashMap<String, Any>): Action? {
//    println("Type: ${json.get("type")}")
//    when (json) {
////        Int -> {}
////        String -> {}
//        LinkedHashMap<String, Any>() -> println("Action is a hash map ${json}")
//        LinkedHashMap<Any, Any>() -> println("Action is a any hash map ${json}")
//        else -> {
//            println("Unknown type")
//            println(json.javaClass.name)                 // double
//            println(json.javaClass.kotlin)               // class kotlin.Double
//            println(json.javaClass.kotlin.qualifiedName)
//
//        }
//    }
//    return null
//}

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
//        ActionType.SETUP_GAME -> fromJson<SetupGame>(json)
//        ActionType.RESET_GAME -> fromJson<ResetGame>(json)
//        ActionType.SETUP_CARDS -> fromJson<SetupCards>(json)
//        ActionType.CARD_PRESSED -> fromJson<CardPressed>(json)
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

//inline fun <reified T> toJSON(message: T): String {
//    val jsonAdapter = getMoshiBuilder().adapter(T::class.java)
//    return jsonAdapter.toJson(message)
//}
//
//private inline fun <reified T> fromJson(json: String): T? {
//    val jsonAdapter = getMoshiBuilder().adapter(T::class.java)
//    return jsonAdapter.fromJson(json)
//}
