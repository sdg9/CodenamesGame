package com.gofficer.codenames.network.interfaces

interface IResponse {
    fun accept(processor: IResponseProcessor)
}