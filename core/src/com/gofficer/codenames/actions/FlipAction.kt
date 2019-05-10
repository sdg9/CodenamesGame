package com.gofficer.codenames.actions

import com.badlogic.gdx.scenes.scene2d.Action

class FlipAction {

    companion object {
        fun flipOut(x: Float, width: Float, duration: Float): Action {
           return object : Action() {
               var left = duration

               override fun act(delta: Float): Boolean {
                   left -= delta
                   if (left <= 0) {
                       actor.x = x + width / 2
                       actor.width = 0f
                       return true
                   }
                   val tmpWidth = width * (left / duration)
                   actor.x = x + (width / 2 - tmpWidth / 2)
                   actor.width = tmpWidth
                   return false
               }
           }
       }

        fun flipIn(x: Float, width: Float, duration: Float): Action {
        return object : Action() {
            var done = 0f

            override fun act(delta: Float): Boolean {
                done += delta
                if (done >= duration) {
                    actor.x = x
                    actor.width = width
                    return true
                }
                val tmpWidth = width * (done / duration)
                actor.x = x + (width / 2 - tmpWidth / 2)
                actor.width = tmpWidth
                return false
            }
        }
    }
    }

}