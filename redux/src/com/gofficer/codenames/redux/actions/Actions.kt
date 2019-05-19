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

// TODO move somewhere logical (server and client need to be in sync)
data class ClientOptions(
    var auth: String?,
    var requestId: Int?,
    var sessionId: String?
)

sealed class NetworkAction: BaseAction {
    var isFromServer: Boolean = false
}

// GamePlay Action

data class TouchCard(val id: Int): NetworkAction()

data class SetupCards(val cards: List<Card>) : NetworkAction()

class ResetGame : NetworkAction()

class SetupGame : NetworkAction()

data class SetState(val state: GameState): NetworkAction()

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
            SubProtocol.SETUP_CARDS -> MoshiPack.unpack<SetupCards>(message)
            SubProtocol.RESET_GAME -> MoshiPack.unpack<ResetGame>(message)
            SubProtocol.SETUP_GAME -> MoshiPack.unpack<SetupGame>(message)
            SubProtocol.SET_STATE -> MoshiPack.unpack<SetState>(message)
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
        is SetupCards -> pack(protocol, SubProtocol.SETUP_CARDS, action)
        is ResetGame -> pack(protocol, SubProtocol.RESET_GAME, action)
        is SetupGame -> pack(protocol, SubProtocol.SETUP_GAME, action)
        is SetState -> pack(protocol, SubProtocol.SET_STATE, action)
    }
}

fun bytesToHexString(byteArray: ByteArray?): String {
    val pattern: Regex = "(\\S{2})".toRegex()
    return Hex.encodeHexString(byteArray).replace(pattern, "$1 ")
}
