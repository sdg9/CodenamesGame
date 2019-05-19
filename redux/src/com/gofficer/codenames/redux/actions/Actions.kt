package com.gofficer.codenames.redux.actions

import com.gofficer.codenames.redux.models.Card
import gofficer.codenames.redux.game.GameState

/**
 * Generic Actions:
 *
 *
 * Gameplay Actions:
 * SetupGame
 * CardPressed
 *
 */

// TODO move somewhere logical (server and client need to be in sync)
data class ClientOptions(
    var auth: String?,
    var requestId: Int?,
    var sessionId: String?
)


// Action all actions must extend to be valid
interface BaseAction

// Actions that should not be sent over the wire
interface LocalAction : BaseAction

// Actions that should be sent over the wire
sealed class NetworkAction: BaseAction {
    var isFromServer: Boolean = false
}

// LOCAL ACTIONS
data class ChangeScene(val screenName: String) : LocalAction


// NETWORK ACTIONS

data class TouchCard(val id: Int): NetworkAction()

data class SetupCards(val cards: List<Card>) : NetworkAction()

class ResetGame : NetworkAction()

class SetupGame : NetworkAction()

data class SetState(val state: GameState): NetworkAction()
