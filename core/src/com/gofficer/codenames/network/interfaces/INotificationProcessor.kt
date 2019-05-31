package com.gofficer.codenames.network.interfaces

import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.network.notification.RemoveEntity

interface INotificationProcessor {

    abstract fun defaultProcess(notification: INotification)

    abstract fun processNotification(notification: EntityUpdate)

    abstract fun processNotification(notification: RemoveEntity)
}