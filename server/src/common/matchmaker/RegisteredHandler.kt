package com.gofficer.colyseus.server.matchmaker

import com.gofficer.colyseus.server.Event
import common.Room
import common.RoomListener


//class RegisteredHandler: Event {
////
////}

//data class RegisteredHandler(val getClass: Any, val options: Object?) {
//    companion object : Event<RegisteredHandler>()
//
//    fun emit() = Companion.emit(this)
//}

//data class RegisteredHandler<T>(val factory: () -> T, val options: Object?) {
//    companion object : Event<RegisteredHandler<*>>()
//    fun emit(s: String, room: Room<*>) = Companion.emit(this)
//}
data class RegisteredHandler<T>(val factory: () -> T, val options: Object?, val listener: RoomListener?) {
    companion object : Event<RegisteredHandler<*>>()
//    fun emit(s: String, room: Room<*>) = Companion.emit(this)
}
