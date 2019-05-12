package com.example.common

import io.ktor.util.generateNonce


fun generateId(): String {
    return generateNonce()
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