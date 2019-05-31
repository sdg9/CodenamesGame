package com.gofficer.codenames.network.server

import com.artemis.World
import com.badlogic.gdx.utils.TimeUtils
import com.esotericsoftware.minlog.Log
import com.gofficer.codenames.GameServer
import com.gofficer.codenames.network.interfaces.DefaultRequestProcessor
import com.gofficer.codenames.systems.server.NetworkManager
import java.util.*

/**
 * Every packet received from users will be processed here
 */
class ServerRequestProcessor(val server: GameServer) : DefaultRequestProcessor() {

//    private val world: World?
//        get() = server.world
//
//    private val networkManager: NetworkManager
//        get() = server.networkManager

//    private val mapManager: MapManager
//        get() = server.getMapManager()
//
//    private val worldManager: WorldManager
//        get() = server.getWorldManager()
//
//    private val itemManager: ItemManager
//        get() = server.getItemManager()
//
//    private val spellManager: SpellManager
//        get() = server.getSpellManager()
//
//    private fun getCombatSystem(type: AttackType): CombatSystem {
//        return if (type.equals(AttackType.PHYSICAL)) {
//            server.getCombatManager()
//        } else server.getCombatManager()
//        // TODO
//    }
//
//    private fun getArea(worldPos: WorldPos, range: Int /*impar*/): List<WorldPos> {
//        val positions = ArrayList<WorldPos>()
//        val i = range / 2
//        for (x in worldPos.x - i..worldPos.x + i) {
//            for (y in worldPos.y - i..worldPos.y + i) {
//                positions.add(WorldPos(x, y, worldPos.map))
//            }
//        }
//        return positions
//    }


}
