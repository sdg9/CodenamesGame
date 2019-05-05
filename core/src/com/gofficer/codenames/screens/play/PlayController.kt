package com.gofficer.codenames.screens.play

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions.*
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.Align
import com.gofficer.codenames.actions.FlipAction
import com.gofficer.codenames.actors.Card
import com.gofficer.codenames.game.CodenamesGame
import com.gofficer.codenames.actors.SlideButton
import com.gofficer.codenames.config.GameConfig
import com.gofficer.codenames.utils.clearScreen
import com.gofficer.codenames.utils.logger


class PlayController {

    companion object {
        @JvmStatic
        private val log = logger<PlayController>()
    }

    private var cardAText = "Hello"



    // == public functions ==
    fun update(delta: Float) {
//        if (gameOver) {
//            return
//        }
//
//        // update game world
//        var xSpeed = 0f
//
//        when {
//            Input.Keys.RIGHT.isKeyPressed() -> xSpeed = Player.MAX_X_SPEED
//            Input.Keys.LEFT.isKeyPressed() -> xSpeed = -Player.MAX_X_SPEED
//        }
//
//        player.x += xSpeed
//
//        blockPlayerFromLeavingTheWorld()
//
//        createNewObstacle(delta)
//        updateObstacles()
//        removePassedObstacles()
//
//        updateScore(delta)
//        updateDisplayScore(delta)
//
//        if (isPlayerCollidingWithObstacle()) {
//            log.debug("collision detected")
//            lives--
//
//            when {
//                gameOver -> log.debug("Game Over!")
//                else -> restart()
//            }
//        }
    }
}
