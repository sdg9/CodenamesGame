package com.example.common.presence

import common.Room
import org.redisson.api.RFuture
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask
import kotlin.reflect.jvm.jvmName

class LocalPresence: Presence {


    private val logger by lazy { LoggerFactory.getLogger(LocalPresence::class.jvmName) }

    val channels : MutableMap<String, Boolean> = mutableMapOf()

    val data : MutableMap<String, MutableList<String>> = mutableMapOf()

    val hash : MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    val keys: MutableMap<String, String> = mutableMapOf()

    private val timeouts : MutableMap<String, TimerTask> = mutableMapOf()


    override fun subscribe(topic: String, callback: (message: String) -> Unit): Presence {
        this.channels[topic] = true
        return this
    }

    override fun unsubscribe(topic: String): Presence {
        this.channels[topic] = false
        return this
    }

    override fun publish(topic: String, data: Any): Presence {
        return this
    }

    override suspend fun exists(roomId: String): Boolean {
        return this.channels[roomId] != null
    }

    override fun setex(key: String, value: String, seconds: Long): RFuture<Void>? {
        // ensure previous timeout is clear before setting another
        this.timeouts[key]?.cancel()
        this.keys[key] = value
        this.timeouts[key] = Timer().schedule(seconds * 1000){
            keys.remove(key)
        }
        return null
    }

    override fun get(key: String): String? {
        return this.keys[key]
    }

    override fun del(key: String) {
        this.data?.remove(key)
        this.hash?.remove(key)
    }

    override fun sadd(key: String, value: String) {
        logger.debug("sadd: $key, $value")
        if (this.data[key] == null) {
            this.data[key] = mutableListOf()
        }

        if (this.data[key]?.indexOf(value) == -1) {
            this.data[key]?.add(value)
        }
    }

    override fun smembers(key: String): MutableList<String>? {
        logger.debug("smembers: $key")
        return this.data[key]
    }

    override fun srem(key: String, value: Any) {
        this.data[key]?.indexOf(value)?.let {
            this.data[key]?.removeAt(it)
        }
    }

    override fun scard(key: String): Int? {
        return this.data[key]?.size
    }

    override fun hset(roomId: String, key: String, value: String) {
        if (this.hash[roomId] == null) {
            this.hash[roomId] = mutableMapOf()
        }

        this.hash[roomId]?.set(key, value)
    }

    override suspend fun hget(roomId: String, key: String): String? {
        return this.hash[roomId]?.get(key)
    }

    override suspend fun hlen(roomId: String): Int? {
        return this.hash[roomId]?.keys?.size
    }

    override fun hdel(roomId: String, key: String) {
        this.hash[roomId]?.remove(key)
    }

    override fun incr(key: String) {
        // TODO do we really need string or int?
//        if (this.keys[key] == null) {
//            this.keys[key] = 0
//        }
        TODO("not implemented")
    }

    override fun decr(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}