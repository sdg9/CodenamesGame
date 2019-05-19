package com.gofficer.colyseus.network

class Protocol {

    companion object {

        const val WS_CLOSE_CONSENTED = 4000;

        // User-related (1~8)
        const val USER_ID = 1

        // Room-related (9~19)
        const val JOIN_REQUEST = 9
        const val JOIN_ROOM = 10
        const val JOIN_ERROR = 11
        const val LEAVE_ROOM = 12
        const val ROOM_DATA = 13
        const val ROOM_STATE = 14
        const val ROOM_STATE_PATCH = 15

        // Match-making related (20~29)
        const val ROOM_LIST = 20

        // Generic messages (50~60)
        const val BAD_REQUEST = 50

        // WebSocket error codes
        const val WS_SERVER_DISCONNECT = 4201
        const val WS_TOO_MANY_CLIENTS = 4202
    }
}