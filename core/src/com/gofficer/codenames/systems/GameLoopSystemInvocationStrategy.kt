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

package com.gofficer.codenames.systems

import com.artemis.BaseSystem
import com.artemis.SystemInvocationStrategy
import com.artemis.utils.Bag
import com.badlogic.gdx.utils.TimeUtils
import com.gofficer.codenames.GameClient
import com.gofficer.codenames.GameSettings
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

    private inner class SystemAndProfiler(internal var system: BaseSystem, internal var profiler: SystemProfiler)

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

        val profilerName = "$prepender.${system.javaClass.simpleName}"

        val profiler = SystemProfiler()
        profiler.initialize(system, world, profilerName)

        val perfStat = PerfStat(profilerName)

        val hash = if (isServer) {
            serverPerfCounter
        } else {
            clientPerfCounter
        }

        hash[system] = perfStat

        return SystemAndProfiler(system, profiler)
    }

    private fun processProfileSystem(systemAndProfiler: SystemAndProfiler) =
            systemAndProfiler.apply {
                profiler.start()
                system.process()
                profiler.stop()

                profiler.counter.tick()
            }

    override fun initialize() {
        //convert from nanos to millis then to seconds, to get fractional second dt
        world.setDelta(TimeUtils.nanosToMillis(nsPerTick) / 1000.0f)

        addSystems(systems)
    }

    override fun process() {
        if (!isServer) {
            //    frameProfiler.start()
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

        //try {
        //    int sleep = (int)Math.max(newTime + CLIENT_FIXED_TIMESTEP - TimeUtils.millis()/1000.0, 0.0);
        //    Thread.sleep(sleep);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}

        //float alpha = (float) accumulator / nsPerTick;

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

        if (GameSettings.profilerEnabled) {
            updateProfilers()

            //defunct
            // printProfilerStats()
        }

        if (!isServer) {
            //frameProfiler.stop()
        }
    }

    private fun updateProfilers() {
        if (isServer) {
            synchronized(serverPerfCounter) {
                updateProfilerStatsForSystems(logicSystems, serverPerfCounter)
            }
            //obviously, no rendering systems here..
        } else {
            //client
            updateProfilerStatsForSystems(renderSystems, clientPerfCounter)
            updateProfilerStatsForSystems(logicSystems, clientPerfCounter)
        }
    }

    private fun updateProfilerStatsForSystems(systems: List<SystemAndProfiler>, hash: HashMap<BaseSystem, PerfStat>) {
        for (systemAndProfiler in systems) {
            val counter = systemAndProfiler.profiler.counter

            hash[systemAndProfiler.system]!!.apply {
                timeMin = counter.time.min * 1000f
                timeMax = counter.time.max * 1000f
                timeCurrent = counter.time.latest * 1000f
                timeAverage = counter.time.average * 1000f
            }
        }
    }

    class PerfStat(val systemName: String, var timeMin: Float = 0f, var timeMax: Float = 0f,
                   var timeAverage: Float = 0f, var timeCurrent: Float = 0f,
                   var loadMin: Float = 0f, var loadMax: Float = 0f,
                   var loadAverage: Float = 0f)

    //separated to reduce theoretical thread lock-stepping/stuttering
    //since client will want to reach in and grab server perf stats,
    //which will require synchronization to do so reliably (or you could
    //be grabbing half stats from prior frames)

    //todo should be replaced with concurrent hashmap?
    val serverPerfCounter = hashMapOf<BaseSystem, PerfStat>()

    val clientPerfCounter = hashMapOf<BaseSystem, PerfStat>()

    private fun printProfilerStats() {
        if (isServer) {
            serverPerfCounter.forEach { baseSystem, perfStat ->
                val s = with(perfStat) {
                    """tmin: ${timeMin.format()}
                    tmax: ${timeMax.format()}
                    tavg: ${timeAverage.format()}
                    lmin: ${loadMin.format()}
                    lmin: ${loadMax.format()}
                    lmin: ${loadAverage.format()}
                    """
                }
                println(s)
            }
        } else {
            /*
            clientPerfCounter.forEach { baseSystem, perfStat ->
                val s = """tmin: ${perfStat.timeMin.format()}
                    tmax: ${perfStat.timeMax.format()}
                    tavg: ${perfStat.timeAverage.format()}
                    lmin: ${perfStat.loadMin.format()}
                    lmin: ${perfStat.loadMax.format()}
                    lmin: ${perfStat.loadAverage.format()}
                    """
                println(s)
            }
            */
        }
    }
}

