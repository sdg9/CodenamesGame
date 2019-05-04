package com.gofficer.codenames.utils

import com.badlogic.gdx.assets.AssetDescriptor

/**
 * @author goran on 5/11/2017.
 */

inline fun <reified T : Any> assetDescriptor(fileName: String) = AssetDescriptor<T>(fileName, T::class.java)