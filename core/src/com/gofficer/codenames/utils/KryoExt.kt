package com.gofficer.codenames.utils

import com.esotericsoftware.kryo.Kryo

inline fun <reified T : Any> Kryo.registerClass() {
    this.register(T::class.java)
}