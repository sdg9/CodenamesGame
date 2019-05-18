package com.gofficer.colyseus

class Protocol {

    companion object {

        val WS_CLOSE_CONSENTED = 4000;


        // User-related (1~8)
        val USER_ID = 1

        // Room-related (9~19)
        val JOIN_REQUEST = 9
        val JOIN_ROOM = 10
        val JOIN_ERROR = 11
        val LEAVE_ROOM = 12
        val ROOM_DATA = 13
        val ROOM_STATE = 14
        val ROOM_STATE_PATCH = 15

        // Match-making related (20~29)
        val ROOM_LIST = 20

        // Generic messages (50~60)
        val BAD_REQUEST = 50

        // WebSocket error codes
        val WS_SERVER_DISCONNECT = 4201
        val WS_TOO_MANY_CLIENTS = 4202
    }
}