package com.gofficer.codenames.network.notification

import com.gofficer.codenames.network.interfaces.INotification
import com.gofficer.codenames.network.interfaces.INotificationProcessor

class RemoveEntity : INotification {

    var entityId: Int = 0

    constructor() {}

    constructor(entityId: Int) {
        this.entityId = entityId
    }

    override fun accept(processor: INotificationProcessor) {
        processor.processNotification(this)
    }
}