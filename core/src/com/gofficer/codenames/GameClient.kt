package com.gofficer.codenames

/**
 * This class is responsible for sending generic outbound messages to the server, as well as handling an Entity ID
 * pairmap that it uses to create and track entities sent from the server. Whenever a message comes in from the server
 * that has a component referencing an unknown Entity, the Entity is created and added to the world automatically.
 */
class GameClient {


    var world: GameWorld? = null

}