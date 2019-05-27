/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.HashMap

import org.junit.runner.notification.RunNotifier
import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import org.mockito.Mockito.mock

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration

class GdxTestRunner @Throws(InitializationError::class)
constructor(klass: Class<*>) : BlockJUnit4ClassRunner(klass), ApplicationListener {

    private val invokeInRender = HashMap<FrameworkMethod, RunNotifier>()

    init {
        val conf = HeadlessApplicationConfiguration()

        HeadlessApplication(this, conf)
        Gdx.gl = mock(GL20::class.java)
    }

    override fun create() {}

    override fun resume() {}

    override fun render() {
        synchronized(invokeInRender) {
            for ((key, value) in invokeInRender) {
                super.runChild(key, value)
            }
            invokeInRender.clear()
        }
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun dispose() {}

    override fun runChild(method: FrameworkMethod, notifier: RunNotifier) {
        synchronized(invokeInRender) {
            // add for invoking in render phase, where gl context is available
            invokeInRender.put(method, notifier)
        }
        // wait until that test was invoked
        waitUntilInvokedInRenderMethod()
    }

    /**
     *
     */
    private fun waitUntilInvokedInRenderMethod() {
        try {
            while (true) {
                Thread.sleep(10)
                synchronized(invokeInRender) {
                    if (invokeInRender.isEmpty())
                        return
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

}