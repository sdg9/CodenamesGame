package com.gofficer.codenames


import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.managers.PlayerManager
import com.artemis.managers.TagManager
import com.artemis.systems.EntityProcessingSystem
import com.artemis.utils.IntBag
import com.artemis.utils.reflect.ClassReflection
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.RandomXS128
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.TimeUtils
import com.gofficer.codenames.components.CardComponent
import ktx.assets.file

@Suppress("NOTHING_TO_INLINE")

/**
 * The main world, shared between both client and server, core to a lot of basic
 * shared functionality, as well as stuff that doesn't really belong elsewhere,
 * creates the artemis world and handles processing, as well as shutting down
 * (when told to do so)

 * @param client
 *         never null..
 *
 * @param server
 *          null if it is only a client, if both client and server are valid, the
 *          this is a local hosted server, (aka singleplayer, or self-hosting)
 */
class GameWorld
    (var client: OreClient?,
     var server: OreServer?,
     var worldInstanceType: GameWorld.WorldInstanceType) {
    fun init() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun process() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private lateinit var tagManager: TagManager

    private lateinit var mCard: ComponentMapper<CardComponent>

    /**
     * who owns/is running this exact world instance. If it is the server, or a client.
     * Note that if the connection type is only a client, obviously a server
     * world type will never exist
     */
    enum class WorldInstanceType {
        //strictly a client. join only
        Client,
        //dedicated server
        Server,
        //it's a client that also happens to be hosting a game (server)
        ClientHostingServer
    }

    val isServer = worldInstanceType == WorldInstanceType.Server
    val isClient = worldInstanceType == WorldInstanceType.Client ||
            worldInstanceType == WorldInstanceType.ClientHostingServer

}



