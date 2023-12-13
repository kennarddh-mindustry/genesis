package kennarddh.genesis.common.handlers.server

import arc.Core
import arc.util.Log
import arc.util.OS
import arc.util.Reflect
import arc.util.Timer
import kennarddh.genesis.core.commands.annotations.Command
import kennarddh.genesis.core.commands.annotations.ServerSide
import kennarddh.genesis.core.commands.result.CommandResult
import kennarddh.genesis.core.commands.result.CommandResultStatus
import kennarddh.genesis.core.handlers.Handler
import mindustry.Vars
import mindustry.core.GameState
import mindustry.core.Version
import mindustry.game.Gamemode
import mindustry.gen.Call
import mindustry.maps.Map
import mindustry.maps.MapException
import mindustry.net.Administration
import mindustry.server.ServerControl


class ServerHandler : Handler() {
    @Command(["host"])
    @ServerSide
    fun host(): CommandResult {
        if (Vars.state.isGame) {
            return CommandResult("Already hosting. Type 'stop' to stop hosting first.", CommandResultStatus.Failed)
        }

        Core.app.post {
            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()
        }

        val preset = Gamemode.survival
        val result: Map = Vars.maps.shuffleMode.next(preset, Vars.state.map)
        Log.info("Randomized next map to be @.", result.plainName())

        Log.info("Loading map...")

        Core.app.post {
            Vars.logic.reset()

            ServerControl.instance.lastMode = preset

            Core.settings.put("lastServerMode", ServerControl.instance.lastMode.name)
        }

        try {
            Core.app.post {
                Vars.world.loadMap(result, result.applyRules(ServerControl.instance.lastMode))
                Vars.state.rules = result.applyRules(preset)
                Vars.logic.play()

                Log.info("Map loaded.")

                Vars.netServer.openServer()

                if (Administration.Config.autoPause.bool()) {
                    Vars.state.set(GameState.State.paused)

                    Reflect.set(ServerControl.instance, "autoPaused", true)
                }
            }

            return CommandResult("Host success")
        } catch (e: MapException) {
            return CommandResult("${e.map.plainName()}: ${e.message}", CommandResultStatus.Failed)
        }
    }

    @Command(["version"])
    @ServerSide
    fun version(): CommandResult {
        return CommandResult(
            """
            Version: Mindustry ${Version.number}-${Version.modifier} ${Version.type} / build ${Version.build} ${if (Version.revision == 0) "" else ".${Version.revision}"}
            Java Version: ${OS.javaVersion}
            """.trimIndent()
        )
    }

    @Command(["exit"])
    @ServerSide
    fun exit(): CommandResult {
        Core.app.post {
            Vars.net.dispose()
            Core.app.exit()
        }

        return CommandResult("Server shutdown")
    }

    @Command(["stop"])
    @ServerSide
    fun stop(): CommandResult {
        Core.app.post {
            Vars.net.closeServer()

            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()

            Vars.state.set(GameState.State.menu)
        }

        return CommandResult("Stopped server.")
    }

    @Command(["say"])
    @ServerSide
    fun say(message: String): CommandResult {
        if (!Vars.state.isGame)
            return CommandResult("Not hosting. Host a game first.", CommandResultStatus.Failed)

        Core.app.post {
            Call.sendMessage("[scarlet][[Server]:[] $message")
        }

        return CommandResult("Server: $message")
    }

}