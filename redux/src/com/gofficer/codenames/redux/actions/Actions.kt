package com.gofficer.codenames.redux.actions

import com.gofficer.codenames.redux.models.Card


interface Action

interface NetworkAction : Action {
    var isFromServer: Boolean
}

data class NetworkMessage(val type: String?, val payload: NetworkAction)

data class ChangeScene(val screenName: String) : Action


data class SetupCards(val cards: List<Card>) : Action


class ResetGame(override var isFromServer: Boolean = false) : NetworkAction

class SetupGame(override var isFromServer: Boolean = false) : NetworkAction