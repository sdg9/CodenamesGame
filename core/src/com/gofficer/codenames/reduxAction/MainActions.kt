package com.gofficer.codenames.reduxAction

import com.gofficer.redux.Action

data class ChangeColor(val red: Float, val green: Float, val blue: Float): Action
