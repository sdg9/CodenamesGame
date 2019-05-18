package com.gofficer.colyseus.server.presence

import org.redisson.api.RFuture

interface Presence {
    fun subscribe(topic: String, callback: (message: String) -> Unit) : Presence
    fun unsubscribe(topic: String) : Presence
    fun publish(topic: String, data: Any) : Presence

    suspend fun exists(roomId: String): Boolean

    fun setex(key: String, value: String, seconds: Long): RFuture<Void>?;
    fun get(key: String) : String?

    fun del(key: String): Unit;
    fun sadd(key: String, value: String);
    fun smembers(key: String) : List<String>?
    fun srem(key: String, value: Any);
    fun scard(key: String): Int?;

    fun hset(roomId: String, key: String, value: String);
    suspend fun hget(roomId: String, key: String): String?
    fun hdel(roomId: String, key: String);
    suspend fun hlen(roomId: String): Int?

    fun incr(key: String);
    fun decr(key: String);
}