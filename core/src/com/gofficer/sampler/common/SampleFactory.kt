package com.gofficer.sampler.common

import com.badlogic.gdx.utils.reflect.ClassReflection

/**
 * @author goran on 29/10/2017.
 */
object SampleFactory {

    fun newSample(name: String): SampleBase {
        val info = SampleInfos.find(name)
        return ClassReflection.newInstance(info?.clazz)
    }
}