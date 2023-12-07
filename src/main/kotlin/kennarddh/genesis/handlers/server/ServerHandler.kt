package kennarddh.genesis.handlers.server

import arc.Core
import arc.util.Log
import arc.util.Reflect
import arc.util.Timer
import kennarddh.genesis.commands.annotations.ClientSide
import kennarddh.genesis.commands.annotations.Command
import kennarddh.genesis.commands.annotations.ServerSide
import kennarddh.genesis.commands.result.CommandResult
import kennarddh.genesis.commands.result.CommandResultStatus
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
    fun host(): CommandResult {
        if (Vars.state.isGame) {
            return CommandResult("Already hosting. Type 'stop' to stop hosting first.", CommandResultStatus.Failed)
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

            return CommandResult("Host success")
        } catch (e: MapException) {
            return CommandResult("${e.map.plainName()}: ${e.message}", CommandResultStatus.Failed)
        }
    }

    @Command("ping")
    @ClientSide
    fun ping(player: Player): CommandResult {
        return CommandResult("Pong!")
    }

    @Command("log")
    @ClientSide
    @ServerSide
    fun log(): CommandResult {
        Log.info("Log.")
        
        return CommandResult("Log sucess.")
    }
}