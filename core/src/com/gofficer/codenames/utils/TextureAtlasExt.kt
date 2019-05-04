package com.gofficer.codenames.utils

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion

/**
 * @author goran on 5/11/2017.
 */

inline operator fun TextureAtlas.get(regionName: String) : TextureRegion? = findRegion(regionName)