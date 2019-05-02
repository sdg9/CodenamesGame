package com.gofficer.codenames.actions

import com.badlogic.gdx.scenes.scene2d.Action

class FlipAction {

    companion object {
        fun flipOut(x: Float, width: Float, duration: Float): Action {
           return object : Action() {
               internal var left = duration

               override fun act(delta: Float): Boolean {
                   left -= delta
                   if (left <= 0) {
                       actor.setX(x + width / 2)
                       actor.setWidth(0f)
                       return true
                   }
                   val tmpWidth = width * (left / duration)
                   actor.setX(x + (width / 2 - tmpWidth / 2))
                   actor.setWidth(tmpWidth)
                   return false
               }
           }
       }

        fun flipIn(x: Float, width: Float, duration: Float): Action {
        return object : Action() {
            internal var done = 0f

            override fun act(delta: Float): Boolean {
                done += delta
                if (done >= duration) {
                    actor.setX(x)
                    actor.setWidth(width)
                    return true
                }
                val tmpWidth = width * (done / duration)
                actor.setX(x + (width / 2 - tmpWidth / 2))
                actor.setWidth(tmpWidth)
                return false
            }
        }
    }
    }

}