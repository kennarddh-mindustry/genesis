package kennarddh.genesis.handlers.server

import arc.Core
import arc.util.Log
import arc.util.Reflect
import arc.util.Timer
import kennarddh.genesis.commands.annotations.ClientSide
import kennarddh.genesis.commands.annotations.Command
import kennarddh.genesis.commands.annotations.ServerSide
import kennarddh.genesis.handlers.Handler
import mindustry.Vars
import mindustry.core.GameState
import mindustry.game.Gamemode
import mindustry.gen.Player
import mindustry.maps.Map
import mindustry.maps.MapException
import mindustry.net.Administration
import mindustry.server.ServerControl

class ServerHandler : Handler() {
    override fun onInit() {
        println("Server handler init")
    }

    @Command("host")
    @ServerSide
    fun host() {
        if (Vars.state.isGame) {
            Log.err("Already hosting. Type 'stop' to stop hosting first.")
            return
        }

        // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
        Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()

        val preset = Gamemode.survival
        val result: Map = Vars.maps.shuffleMode.next(preset, Vars.state.map)
        Log.info("Randomized next map to be @.", result.plainName())

        Log.info("Loading map...")

        Vars.logic.reset()

        ServerControl.instance.lastMode = preset

        Core.settings.put("lastServerMode", ServerControl.instance.lastMode.name)

        try {
            Vars.world.loadMap(result, result.applyRules(ServerControl.instance.lastMode))
            Vars.state.rules = result.applyRules(preset)
            Vars.logic.play()

            Log.info("Map loaded.")

            Vars.netServer.openServer()

            if (Administration.Config.autoPause.bool()) {
                Vars.state.set(GameState.State.paused)

                Reflect.set(ServerControl.instance, "autoPaused", true)
            }
        } catch (e: MapException) {
            Log.err("@: @", e.map.plainName(), e.message)
        }
    }

    @Command("ping")
    @ClientSide
    fun ping(player: Player) {
        player.sendMessage("Pong!")
    }

    @Command("log")
    @ClientSide
    @ServerSide
    fun log() {
        Log.info("Log!")
    }
}