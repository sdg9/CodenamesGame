package com.gofficer.codenames.network.interfaces

interface IRequest {

    fun accept(processor: IRequestProcessor, connectionId: Int)

}
