package com.gofficer.codenames.network.interfaces

import com.gofficer.codenames.Network

interface IRequestProcessor {

    fun processRequest(request: Network.Client.JoinRoomRequest, connectionId: Int)
}