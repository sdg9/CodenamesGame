package com.gofficer.codenames.utils

import com.badlogic.gdx.utils.Logger

inline fun <reified T : Any> logger(): Logger = Logger(T::class.java.simpleName, Logger.DEBUG)
//fun <T: Any> logger(clazz: Class<T>): Logger = Logger(clazz.simpleName, Logger.DEBUG)
