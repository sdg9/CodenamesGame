package com.gofficer.sampler.utils

import com.badlogic.gdx.utils.Logger

/**
 * @author goran on 26/10/2017.
 */

// fun <T : Any> logger(clazz: Class<T>): Logger = Logger(clazz.simpleName, Logger.DEBUG)

inline fun <reified T : Any> logger(): Logger = Logger(T::class.java.simpleName, Logger.DEBUG)