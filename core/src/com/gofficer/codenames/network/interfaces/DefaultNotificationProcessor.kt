package com.gofficer.codenames.network.interfaces

import com.gofficer.codenames.network.notification.EntityUpdate
import com.gofficer.codenames.network.notification.RemoveEntity


open class DefaultNotificationProcessor : INotificationProcessor {

    override fun defaultProcess(notification: INotification) {

    }

    override fun processNotification(notification: EntityUpdate) {

    }

    override fun processNotification(removeEntity: RemoveEntity) {

    }

}
