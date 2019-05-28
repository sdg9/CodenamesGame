package com.gofficer.codenames


import com.badlogic.gdx.utils.TimeUtils

class GameTimer {

    private var lastMs: Long = 0

    //fixme this shouldn't need to get called first, but i've seen things want different behavior
    //e.g. some want to run it right away, others want to wait until it goes by...
    fun reset() {
        lastMs = TimeUtils.millis()
    }

    fun start() {
        reset()
    }

    val currentMs: Long
        get() = TimeUtils.millis()

    /**
     * @param intervalMs the rate/interval to check
     * if the current time is past the previous time
     * by this much.
     *
     * If it is, true is returned and the timer is reset
     * to the current time. False otherwise.
     *
     * @param f executes the optional param if it was surpassed
     */
    fun resetIfExpired(intervalMs: Long, f: (() -> Unit)? = null): Boolean {
        if (currentMs - lastMs > intervalMs) {
            lastMs = currentMs
            f?.invoke()
            return true
        }

        return false
    }

    fun surpassed(intervalMs: Long) = (currentMs - lastMs > intervalMs)

    fun milliseconds(): Long {
        return TimeUtils.timeSinceMillis(lastMs)
    }
}
