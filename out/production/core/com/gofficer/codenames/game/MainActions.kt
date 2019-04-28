package com.gofficer.codenames.game

import com.gofficer.codenames.redux.Action

data class ChangeColor(val red: Float, val green: Float, val blue: Float): Action
