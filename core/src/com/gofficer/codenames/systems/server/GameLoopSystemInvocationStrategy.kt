package com.gofficer.codenames.systems.server

/**
MIT License
Copyright (c) 2016 Shaun Reich <sreich02@gmail.com>
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

import com.artemis.BaseSystem
import com.artemis.SystemInvocationStrategy
import com.artemis.utils.Bag
import com.badlogic.gdx.utils.TimeUtils
import com.gofficer.codenames.utils.RenderSystemMarker
import com.gofficer.codenames.utils.format
import ktx.app.clearScreen
import ktx.log.logger
import java.util.*

class GameLoopSystemInvocationStrategy
/**
 * @param msPerLogicTick
 *         desired ms per tick you want the logic systems to run at.
 *         this doesn't affect rendering as that is unbounded/probably
 *         bounded by libgdx's DesktopLauncher
 */
    (msPerLogicTick: Int, private val isServer: Boolean) : SystemInvocationStrategy() {


    companion object {
        val log = logger<GameLoopSystemInvocationStrategy>()
    }

    //systems marked as indicating to be run only during the logic section of the loop
    private val renderSystems = mutableListOf<SystemAndProfiler>()
    private val logicSystems = mutableListOf<SystemAndProfiler>()

    private inner class SystemAndProfiler(internal var system: BaseSystem)

    private var accumulatorNs: Long = 0

    //delta time
    private val nsPerTick = TimeUtils.millisToNanos(msPerLogicTick.toLong())

    private var currentTimeNs = System.nanoTime()

    //minimum tick to chug along as, when we get really slow.
    //this way we're still rendering even though logic is taking up
    //an overbearing portion of our frame time

    // (1/15)fps * 1000ms
    private val minMsPerFrame: Long = 67
    private val minNsPerFrame = TimeUtils.millisToNanos(minMsPerFrame)

    private fun addSystems(systems: Bag<BaseSystem>) {
        for (system in systems) {
            if (system is RenderSystemMarker) {
                renderSystems.add(createSystemAndProfiler(system))
            } else {
                logicSystems.add(createSystemAndProfiler(system))
            }
        }
    }

    private fun createSystemAndProfiler(system: BaseSystem): SystemAndProfiler {
        val prepender = if (isServer) {
            "server"
        } else {
            "client"
        }

        return SystemAndProfiler(system)
    }

    private fun processProfileSystem(systemAndProfiler: SystemAndProfiler) =
        systemAndProfiler.apply {
            system.process()
        }

    override fun initialize() {
        //convert from nanos to millis then to seconds, to get fractional second dt
        world.setDelta(TimeUtils.nanosToMillis(nsPerTick) / 1000.0f)

        log.debug { "Adding systems $systems" }
        addSystems(systems)
    }

    override fun process() {
        if (!isServer) {
            log.debug { "Not server processing" }
        }

        val newTimeNs = System.nanoTime()
        //nanoseconds
        var frameTimeNs = newTimeNs - currentTimeNs

        if (frameTimeNs > minNsPerFrame) {
            frameTimeNs = minNsPerFrame    // Note: Avoid spiral of death
        }

        currentTimeNs = newTimeNs
        accumulatorNs += frameTimeNs

        while (accumulatorNs >= nsPerTick) {
            /** Process all entity systems inheriting from [RenderSystemMarker]  */
            for (systemAndProfiler in logicSystems) {
                //TODO interpolate before this
                //        processProfileSystem(systemAndProfiler.profiler, systemAndProfiler.system)
                updateEntityStates()
                processProfileSystem(systemAndProfiler)
            }
            updateEntityStates()

            accumulatorNs -= nsPerTick
        }

        //only clear if we have something to render..aka this world is a rendering one (client)
        //else it's a server, and this will crash due to no gl context, obviously
        if (!isServer) {
            clearScreen(.1f, .1f, .1f)
        }

        for (systemAndProfiler in renderSystems) {
            //TODO interpolate this rendering with the state from the logic run, above
            //State state = currentState * alpha +
            //previousState * ( 1.0 - alpha );

            //TODO interpolate before this
            //processProfileSystem(systemAndProfiler.profiler, systemAndProfiler.system)
            processProfileSystem(systemAndProfiler)

            updateEntityStates()
        }

        if (!isServer) {
            //frameProfiler.stop()
        }
    }

}