package kennarddh.genesis.handlers.server

import arc.Core
import arc.util.Log
import arc.util.Reflect
import arc.util.Timer
import kennarddh.genesis.commands.annotations.ClientSide
import kennarddh.genesis.commands.annotations.Command
import kennarddh.genesis.commands.annotations.ServerSide
import kennarddh.genesis.commands.parameters.validations.numbers.Max
import kennarddh.genesis.commands.parameters.validations.numbers.Min
import kennarddh.genesis.commands.result.CommandResult
import kennarddh.genesis.commands.result.CommandResultStatus
import kennarddh.genesis.events.annotations.EventHandler
import kennarddh.genesis.handlers.Handler
import mindustry.Vars
import mindustry.core.GameState
import mindustry.game.EventType
import mindustry.game.Gamemode
import mindustry.gen.Call
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
    fun ping(@Suppress("UNUSED_PARAMETER") player: Player): CommandResult {
        return CommandResult("Pong!")
    }

    @Command("log")
    @ClientSide
    @ServerSide
    fun log(player: Player? = null): CommandResult {
        if (player != null)
            Log.info("Log by ${player.name}.")
        else
            Log.info("Log by Server.")

        return CommandResult("Log success.", colorDependsOnStatus = false)
    }

    @Command("say")
    @ClientSide
    @ServerSide
    fun say(@Suppress("UNUSED_PARAMETER") player: Player? = null, message: String): CommandResult {
        if (!Vars.state.isGame)
            return CommandResult("Not hosting. Host a game first.", CommandResultStatus.Failed)

        Core.app.post {
            Call.sendMessage("[scarlet][[Server]:[] $message")
        }

        Log.info("Server: $message")

        return CommandResult("Say success.")
    }

    @Command("add")
    @ClientSide
    @ServerSide
    fun add(
        @Suppress("UNUSED_PARAMETER") player: Player? = null,
        @Min(0) @Max(100) number1: Int,
        @Min(-10) @Max(10) number2: Int = 1
    ): CommandResult {
        return CommandResult("Result: $number1 + $number2 = ${number1 + number2}")
    }

    @EventHandler
    @Suppress("UNUSED")
    fun onPlayerJoin(playerJoin: EventType.PlayerJoin) {
        println("Player join")
        println(playerJoin)
    }
}