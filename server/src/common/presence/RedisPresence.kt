package com.example.common.presence

import org.redisson.Redisson
import org.redisson.api.RFuture
import java.util.concurrent.TimeUnit


class RedisPresence: Presence {

    var pub = Redisson.create()

    var sub = Redisson.create()

    val defaultBucket = "1"

    val subscriptions : MutableMap<String, Any> = mutableMapOf()

    init {

    }

    override fun subscribe(topic: String, callback: (message: String) -> Unit): Presence {
        this.subscriptions[topic] = { channel: String, message: String ->
            if (channel == topic) {
                callback(message)
            }
        }


        // TODO: Add listener
//            this.sub.lis

//            this.subscribe(topic)
//            return this
        return this
    }

    override fun unsubscribe(topic: String): Presence {
        // TODO remove listener

        this.subscriptions.remove(topic)

        return this
    }

    override fun publish(topic: String, data: Any): Presence {
        //TODO
        return this
    }

    override suspend fun exists(roomId: String): Boolean {
        // TODO
        TODO("not implemented") //T
        return true
    }

    override fun setex(key: String, value: String, seconds: Long): RFuture<Void>? {
        // TODO determine if bucket name should be the key
        val bucket = this.pub.getBucket<String>(key)
        return bucket.setAsync(value, seconds, TimeUnit.SECONDS)
    }

    override fun get(key: String): String? {
        val bucket = this.pub.getBucket<String>(key)
        // TODO make nonblocking
        return bucket.get()
    }

    override fun del(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sadd(key: String, value: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun smembers(key: String): List<String>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun srem(key: String, value: Any) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun scard(key: String): Int? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hset(roomId: String, key: String, value: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun hget(roomId: String, key: String): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hdel(roomId: String, key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun hlen(roomId: String): Int? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun incr(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decr(key: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}