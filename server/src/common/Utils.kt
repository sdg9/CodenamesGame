package com.gofficer.colyseus.server

import io.ktor.util.generateNonce


fun generateId(): String {
//    return generateNonce()
    val size = 9
    val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
    return (1..size)
        .map { allowedChars.random() }
        .joinToString("")
}


open class Event<T> {
    var handlers = listOf<(T) -> Unit>()

    infix fun on(handler: (T) -> Unit) {
        handlers += handler
    }

    fun emit(event: T) {
        for (subscriber in handlers) {
            subscriber(event)
        }
    }
}

fun isValidId(id: String): Boolean {
    // TODO
    return true
}