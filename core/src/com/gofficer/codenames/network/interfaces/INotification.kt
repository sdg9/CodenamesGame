package com.gofficer.codenames.network.interfaces

interface INotification {

    fun accept(processor: INotificationProcessor)
}
