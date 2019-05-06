package com.gofficer.codenames.reduxAction

import com.gofficer.redux.Action

data class CardPressed(val id: Int, val word: String): Action
