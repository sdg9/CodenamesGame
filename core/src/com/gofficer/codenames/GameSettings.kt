package com.gofficer.codenames


import com.beust.jcommander.Parameter

object GameSettings {

    @Parameter(names = arrayOf("--help"), help = true)
    @JvmField
    var help: Boolean = false

    //client options//////
    @Parameter(names = arrayOf("--framerate"), description = "the framerate value to limit the game to. 0 is unlimited")
    @JvmField
    var framerate = 60

    @Parameter(names = arrayOf("--vsync"), description = "vsync enabled.")
    @JvmField
    var vsyncEnabled: Boolean = false

    @Parameter(names = arrayOf("--resizable"), description = "if set, the window will be allowed to be freely resized")
    @JvmField
    var resizable: Boolean = false

    @Parameter(names = arrayOf("--width"), description = "window width")
    @JvmField
    var width = 1600

    @Parameter(names = arrayOf("--height"), description = "window height")
    @JvmField
    var height = 900

    //////////////////////////

    /////////////// server and client network related options
    @Parameter(names = arrayOf("--hostAndJoin"),
        description = "immediately jumps into hosting a server and joining it locally. Basically singleplayer," + " but with other people being able to join, technically.")
    private var hostAndJoin: Boolean = false

    @Parameter(names = arrayOf("--host"),
        description = "Hosts a server. Additional settings that must or can be set are: port")
    private var host: Boolean = false

    @Parameter(names = arrayOf("--join"),
        description = "joins a server. Additional settings that must or can be set are: ip(required), port")
    private var join: Boolean = false

    @Parameter(names = arrayOf("--playerName"), description = "applies only to the client")
    @JvmField
    var playerName = "testplayerNameFromCommandLine"

    @Parameter(names = arrayOf("--port"))
    @JvmField
    var port = Network.PORT

    @Parameter(names = arrayOf("--ip"), description = "applies only to the client")
    @JvmField
    var ip = "localhost"

    @Parameter(names = arrayOf("--networkLog"), description = "enable network (kryonet) debug logging to system out")
    @JvmField
    var networkLog: Boolean = false

    @Parameter(names = arrayOf("--lagMin"),
        description = "emulates a slow network guaranteed to have this much latency. For network debugging.")
    @JvmField
    var lagMinMs = 0

    @Parameter(names = arrayOf("--lagMax"),
        description = "emulates a slow network guaranteed to have less than this much latency. For network " + "debugging.")
    @JvmField
    var lagMaxMs = 0

    /**
     * cheat
     */
    @Parameter(names = arrayOf("--noclip"),
        description = "enable noclip. the server will verify if authorized (if it's a local game server, then " + "it is always authorized.")
    @JvmField
    var noClip: Boolean = false

    @Parameter(names = arrayOf("--generateWorld"),
        description = "Generates the world with default params, outputs to image and immediately exits. For testing.")
    @JvmField
    var generateWorld: Boolean = false

    /// lock movement of player to continue moving right
    @JvmField
    var lockRight: Boolean = false

    /**
     * cheat
     */
    @JvmField
    var speedRun = false

    @Parameter(names = arrayOf("--debugPacketTypeStatistics"),
        description = "enable network debug to stdout for displaying frequencies of each packet received, for both client and server.")
    @JvmField
    var debugPacketTypeStatistics: Boolean = false

    @Parameter(names = arrayOf("--saveLoadWorld"),
        description = "automatically load the world at startup and save it at exit (debug).")
    @JvmField
    var saveLoadWorld: Boolean = false

    @Parameter(names = arrayOf("--flatWorld"),
        description = "create a flat simple world at startup, because it's fast (debug)")
    @JvmField
    var flatWorld: Boolean = false

    @Parameter(names = arrayOf("--disable-gui"),
        description = "disable the gui renderer, for debugging the gl more easily")
    @JvmField
    var debugDisableGui: Boolean = false

    var profilerEnabled = false

    @Parameter(names = arrayOf("--renderDebugServer"),
        description = "render debug server overlay for entity debugging")
    @JvmField
    var renderDebugServer = false

    @Parameter(names = arrayOf("--renderDebugClient"),
        description = "render debug client overlay for entity debugging")
    @JvmField
    var renderDebugClient = false

    val zoomAmount = 0.004f
}