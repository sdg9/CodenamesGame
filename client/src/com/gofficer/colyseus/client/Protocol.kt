package com.gofficer.colyseus.client

object Protocol {
    // User-related (0~10)
    val USER_ID = 1

    // Room-related (10~20)
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
}