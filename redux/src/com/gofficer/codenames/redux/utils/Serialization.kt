package com.gofficer.codenames.redux.utils

import com.daveanthonythomas.moshipack.MoshiPack
import com.gofficer.codenames.redux.actions.*
import com.gofficer.colyseus.network.*
import org.apache.commons.codec.binary.Hex


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
