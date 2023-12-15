package kennarddh.genesis.common.handlers.server

import arc.Core
import arc.util.*
import arc.util.serialization.JsonReader
import arc.util.serialization.JsonValue
import arc.util.serialization.JsonValue.ValueType
import kennarddh.genesis.common.commands.parameters.types.BooleanParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer.IntParameter
import kennarddh.genesis.common.commands.parameters.validations.numbers.GTE
import kennarddh.genesis.core.Genesis
import kennarddh.genesis.core.commands.annotations.ClientSide
import kennarddh.genesis.core.commands.annotations.Command
import kennarddh.genesis.core.commands.annotations.Description
import kennarddh.genesis.core.commands.annotations.ServerSide
import kennarddh.genesis.core.commands.parameters.exceptions.CommandParameterValidationException
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidationDescription
import kennarddh.genesis.core.commands.parameters.validations.parameterValidationDescriptionAnnotationToString
import kennarddh.genesis.core.commands.result.CommandResult
import kennarddh.genesis.core.commands.result.CommandResultStatus
import kennarddh.genesis.core.handlers.Handler
import mindustry.Vars.*
import mindustry.core.GameState
import mindustry.core.Version
import mindustry.game.Gamemode
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.io.JsonIO
import mindustry.maps.Map
import mindustry.maps.MapException
import mindustry.net.Administration
import mindustry.server.ServerControl
import kotlin.math.ceil
import kotlin.math.min
import kotlin.reflect.full.findAnnotation


class ServerHandler : Handler() {
    @Command(["help"])
    @ServerSide
    @ClientSide
    @Description("Display the command list, or get help for a specific command.")
    fun help(player: Player? = null, commandOrPage: String = "0"): CommandResult {
        // TODO: Change default commandOrPage to "1"
        val commandsPerPage = 10

        var page: Int? = null
        var commandName: String? = null

        try {
            page = IntParameter().parse(Int::class, commandOrPage)
        } catch (e: CommandParameterParsingException) {
            commandName = commandOrPage
        }

        val output = StringBuilder()

        val isServer = player == null

        val commands = if (isServer)
            Genesis.commandRegistry.serverCommands
        else
            Genesis.commandRegistry.clientCommands

        if (page != null) {
            output.appendLine("Commands:")

            if (page <= 0)
                return CommandResult("Parameter page must be greater than 0", CommandResultStatus.Failed)

            val maxPage = ceil(commands.size.toDouble() / commandsPerPage).toInt()

            if (page > maxPage)
                return CommandResult(
                    "Help max page is $maxPage",
                    CommandResultStatus.Failed
                )

            commands.subList((page - 1) * commandsPerPage, min(page * commandsPerPage, commands.size)).forEach {
                val name = it.names[0]
                val usage = it.toUsage()

                output.append("\t")

                if (!isServer)
                    output.append("[orange]")

                output.append(if (isServer) Genesis.commandRegistry.serverPrefix else Genesis.commandRegistry.clientPrefix)

                output.append(name)

                if (!isServer)
                    output.append("[lightgray]")

                output.append(' ')
                output.append(usage)

                if (it.brief.isNotEmpty()) {
                    if (usage.isNotEmpty())
                        output.append(' ')

                    if (!isServer)
                        output.append("[gray]")

                    output.append("- ")

                    output.append(it.brief)
                }

                output.append('\n')
            }
        } else {
            val command = commands.find { it.names.contains(commandName) }
                ?: return CommandResult("Command $commandName not found.", CommandResultStatus.Failed)

            val firstCommandName = command.names[0]
            val usage = command.toUsage()

            output.appendLine("Command $firstCommandName:")

            output.append("\t")

            if (!isServer)
                output.append("[orange]")

            output.append(if (isServer) Genesis.commandRegistry.serverPrefix else Genesis.commandRegistry.clientPrefix)

            output.append(firstCommandName)

            if (!isServer)
                output.append("[lightgray]")

            output.append(' ')
            output.append(usage)

            if (command.brief.isNotEmpty()) {
                if (usage.isNotEmpty())
                    output.append(' ')

                if (!isServer)
                    output.append("[gray]")

                output.append("- ")

                output.append(command.brief)
            }

            if (command.description.isNotEmpty() && command.brief != command.description) {
                output.append('\n')
                output.append(command.description)
            }

            output.append('\n')

            command.parametersType.forEach {
                it.validator.forEach { validator ->
                    val validatorDescriptionAnnotation =
                        validator.annotationClass.findAnnotation<ParameterValidationDescription>()

                    if (validatorDescriptionAnnotation != null) {
                        output.append("\t- ")
                        output.appendLine(
                            parameterValidationDescriptionAnnotationToString(
                                validatorDescriptionAnnotation,
                                validator,
                                it.name
                            )
                        )
                    }
                }
            }
        }

        return CommandResult(output.trimEnd('\n').toString())
    }

    @Command(["host"])
    @ServerSide
    @Description("Open the server. Will default to survival and a random map if not specified.")
    fun host(mapName: String? = null, gameMode: String = "survival"): CommandResult {
        if (state.isGame)
            return CommandResult("Already hosting. Type 'stop' to stop hosting first.", CommandResultStatus.Failed)

        Core.app.post {
            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()
        }

        val preset: Gamemode

        // TODO: Enum parameter
        try {
            preset = Gamemode.valueOf(gameMode)
        } catch (error: IllegalArgumentException) {
            return CommandResult("Game mode $gameMode not found.", CommandResultStatus.Failed)
        }

        val map: Map?
        if (mapName != null) {
            map = maps.all().find {
                it.plainName().replace('_', ' ')
                    .equals(Strings.stripColors(mapName).replace('_', ' '), ignoreCase = true)
            }

            if (map == null)
                return CommandResult("Map with name $mapName not found.", CommandResultStatus.Failed)
        } else {
            map = maps.shuffleMode.next(preset, state.map)

            Log.info("Randomized next map to be @.", map.plainName())
        }

        Log.info("Loading map...")

        Core.app.post {
            logic.reset()

            ServerControl.instance.lastMode = preset

            Core.settings.put("lastServerMode", ServerControl.instance.lastMode.name)
        }

        try {
            Core.app.post {
                world.loadMap(map, map.applyRules(ServerControl.instance.lastMode))
                state.rules = map.applyRules(preset)
                logic.play()

                Log.info("Map loaded.")

                netServer.openServer()

                if (Administration.Config.autoPause.bool()) {
                    state.set(GameState.State.paused)

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
    @Description("Displays server version info.")
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
    @Description("Exit the server application.")
    fun exit(): CommandResult {
        Core.app.post {
            net.dispose()
            Core.app.exit()
        }

        return CommandResult("Server shutdown")
    }

    @Command(["stop"])
    @ServerSide
    @Description("Stop hosting the server.")
    fun stop(): CommandResult {
        Core.app.post {
            net.closeServer()

            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()

            state.set(GameState.State.menu)
        }

        return CommandResult("Stopped server.")
    }

    @Command(["maps"])
    @ServerSide
    @Description("Display available maps. Displays only custom maps by default.")
    fun maps(type: String = "custom"): CommandResult {
        var showCustom = false
        var showDefault = false

        when (type) {
            "custom" -> showCustom = true
            "default" -> showDefault = true
            "all" -> {
                showCustom = true
                showDefault = true
            }

            else -> return CommandResult(
                "$type is an invalid type. Possible value are custom, default, all.",
                CommandResultStatus.Failed
            )
        }

        val output = StringBuilder()

        if (!maps.all().isEmpty) {
            val all: MutableList<Map> = mutableListOf()

            if (showCustom) all.addAll(maps.customMaps())
            if (showDefault) all.addAll(maps.defaultMaps())

            if (all.isEmpty() && !showDefault)
                output.appendLine("No custom maps loaded. Set default as the first parameter to show default maps.")
            else {
                output.appendLine("Maps:")

                for (map in all) {
                    val mapName = map.plainName().replace(' ', '_')

                    if (map.custom) {
                        output.appendLine("\t${mapName} (${map.file.name()}): Custom / ${map.width}x${map.height}")
                    } else {
                        output.appendLine("\t${mapName}: Default / ${map.width}x${map.height}")
                    }
                }
            }
        } else
            output.appendLine("No maps found.")

        output.appendLine("Map directory: ${customMapDirectory.file().getAbsoluteFile()}")

        return CommandResult(output.trimEnd('\n').toString())
    }

    @Command(["reloadMaps"])
    @ServerSide
    @Description("Reload all maps from disk.")
    fun reloadMaps(): CommandResult {
        val beforeMapsSize = maps.all().size

        maps.reload()

        val output = if (maps.all().size > beforeMapsSize)
            "${maps.all().size - beforeMapsSize} new map(s) found and reloaded."
        else if (maps.all().size < beforeMapsSize)
            "${beforeMapsSize - maps.all().size} old map(s) deleted."
        else
            "Maps reloaded."

        return CommandResult(output)
    }

    @Command(["status"])
    @ServerSide
    @Description("Display server status.")
    fun status(): CommandResult {
        val output = StringBuilder()

        if (state.isMenu)
            output.appendLine("Status: Server closed")
        else {
            val currentMapName = Strings.capitalize(state.map.plainName())
            val currentWave = state.wave

            output.appendLine("Status:")

            output.appendLine("\tPlaying on map $currentMapName / Wave $currentWave")

            if (state.rules.waves)
                output.appendLine("\t${(state.wavetime / 60).toInt()} seconds until next wave.")

            output.appendLine("\t${Groups.unit.size()}units / ${state.enemies} enemies")
            output.appendLine("\t${Core.graphics.framesPerSecond} TPS, ${Core.app.javaHeap / 1024 / 1024} MB used.")

            if (!Groups.player.isEmpty) {
                output.appendLine("\tPlayers: ${Groups.player.size()}")

                for (player in Groups.player)
                    output.appendLine("\t\t${if (player.admin()) "[A]" else "[P]"} ${player.plainName()} / ${player.uuid()}")
            } else
                output.appendLine("\tNo players connected.")
        }

        return CommandResult(output.trimEnd('\n').toString())
    }

    @Command(["mods", "plugins"])
    @ServerSide
    @Description("Display all loaded mods/plugins.")
    fun mods(): CommandResult {
        val output = StringBuilder()

        if (!mods.list().isEmpty) {
            output.appendLine("Mods:")

            for (mod in mods.list())
                output.appendLine("\t${mod.meta.displayName}: ${mod.meta.name}@${mod.meta.version} ${if (mod.enabled()) "" else " (${mod.state})"}")
        } else
            output.appendLine("No mods found.")

        output.appendLine("Mod directory: ${modDirectory.file().getAbsoluteFile()}")

        return CommandResult(output.trimEnd('\n').toString())
    }

    @Command(["mod", "plugin"])
    @ServerSide
    @Description("Display information about a loaded mod/plugin.")
    fun mod(name: String): CommandResult {
        val output = StringBuilder()

        val mod = mods.list().find { it.meta.name.equals(name, ignoreCase = true) }

        if (mod != null) {
            output.appendLine("Name: ${mod.meta.displayName}")
            output.appendLine("Internal Name: ${mod.name}")
            output.appendLine("Version: ${mod.meta.version}")
            output.appendLine("Author: ${mod.meta.author}")
            output.appendLine("Path: ${mod.file.path()}")
            output.appendLine("Description: ${mod.meta.description}")
        } else
            output.appendLine("No mod with name $name found.")

        return CommandResult(output.trimEnd('\n').toString())
    }

    @Command(["javascript", "js"])
    @ServerSide
    @Description("Run arbitrary Javascript.")
    fun javascript(script: String): CommandResult {
        return CommandResult(mods.getScripts().runConsole(script))
    }

    @Command(["pause"])
    @ServerSide
    @Description("Pause or unpause the game.")
    fun pause(pause: Boolean): CommandResult {
        if (state.isMenu)
            return CommandResult("Cannot pause without a game running.", CommandResultStatus.Failed)

        Reflect.set(ServerControl.instance, "autoPaused", false)

        state.set(if (state.isPaused) GameState.State.playing else GameState.State.paused)

        return CommandResult(if (pause) "Game paused." else "Game unpaused.")
    }

    @Command(["say"])
    @ServerSide
    @Description("Send a message to all players.")
    fun say(message: String): CommandResult {
        if (!state.isGame)
            return CommandResult("Not hosting. Host a game first.", CommandResultStatus.Failed)

        Core.app.post {
            Call.sendMessage("[scarlet][[Server]:[] $message")
        }

        return CommandResult("Server: $message")
    }

    @Command(["rules"])
    @ServerSide
    @Description("List, remove or add global rules. These will apply regardless of map.")
    fun rules(type: String = "list", name: String? = null, value: String? = null): CommandResult {
        when (type) {
            "list" -> {
                if (name != null) return CommandResult(
                    "Name is not required for list",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for list",
                    CommandResultStatus.Failed
                )
            }

            "add" -> {
                if (name == null) return CommandResult(
                    "Name is required for add",
                    CommandResultStatus.Failed
                )

                if (value == null) return CommandResult(
                    "Value is required for add",
                    CommandResultStatus.Failed
                )
            }

            "remove" -> {
                if (name == null) return CommandResult(
                    "Name is required for remove",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for remove",
                    CommandResultStatus.Failed
                )
            }

            else -> return CommandResult(
                "$type is an invalid type. Possible value are list, add, remove",
                CommandResultStatus.Failed
            )
        }

        var commandResultOutput: CommandResult

        val rules = Core.settings.getString("globalrules")
        val base = JsonIO.json.fromJson<JsonValue>(null, rules)

        if (type == "list")
            commandResultOutput = CommandResult("Rules:\n${JsonIO.print(rules)}")
        else {
            val isRemove = type == "remove"

            if (isRemove) {
                commandResultOutput = if (base.has(name)) {
                    base.remove(name)

                    CommandResult("Rule '${name}' removed.")
                } else
                    CommandResult("Rule not defined, so not removed.", CommandResultStatus.Failed)
            } else {
                try {
                    val jsonValue: JsonValue = JsonReader().parse(value)

                    jsonValue.name = name

                    val parent = JsonValue(ValueType.`object`)

                    parent.addChild(jsonValue)

                    JsonIO.json.readField(state.rules, jsonValue.name, parent)

                    if (base.has(jsonValue.name))
                        base.remove(jsonValue.name)

                    base.addChild(name, jsonValue)

                    commandResultOutput = CommandResult("Changed rule: ${jsonValue.toString().replace("\n", " ")}")
                } catch (e: Throwable) {
                    commandResultOutput =
                        CommandResult("Error parsing rule JSON: ${e.message}", CommandResultStatus.Failed)
                }
            }

            Core.settings.put("globalrules", base.toString())
            Call.setRules(state.rules)
        }

        return commandResultOutput
    }

    // TODO: Fill items

    @Command(["playerLimit", "playerlimit"])
    @ServerSide
    @Description("Set the server player limit. 0 to disable limit")
    fun playerLimit(@GTE(0) limit: Int? = null): CommandResult {
        if (limit == null)
            return CommandResult(
                "Player limit is currently ${netServer.admins.playerLimit}.",
            )

        netServer.admins.playerLimit = limit

        return if (limit == 0)
            CommandResult("Player limit disabled.")
        else
            CommandResult("Player limit is now $limit")
    }

    @Command(["config"])
    @ServerSide
    @Description("Configure server settings.")
    fun config(type: String = "list", name: String? = null, value: String? = null): CommandResult {
        when (type) {
            "list" -> {
                if (name != null) return CommandResult(
                    "Name is not required for list",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for list",
                    CommandResultStatus.Failed
                )
            }

            "add" -> {
                if (name == null) return CommandResult(
                    "Name is required for add",
                    CommandResultStatus.Failed
                )

                if (value == null) return CommandResult(
                    "Value is required for add",
                    CommandResultStatus.Failed
                )
            }

            "remove", "get" -> {
                if (name == null) return CommandResult(
                    "Name is required for $type",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for $type",
                    CommandResultStatus.Failed
                )
            }

            else -> return CommandResult(
                "$type is an invalid type. Possible value are list, add, remove, get",
                CommandResultStatus.Failed
            )
        }

        var commandResultOutput: CommandResult

        if (type == "list") {
            val output = StringBuilder()

            output.appendLine("All config values:")

            for (config in Administration.Config.all) {
                output.appendLine("\t| ${config.name}: ${config.get()}")
                output.appendLine("\t| | ${config.description}")
            }

            return CommandResult(output.trimEnd('\n').toString())
        }

        val config = Administration.Config.all.find { it.name.equals(name, ignoreCase = true) }

        if (config != null) {
            if (type == "get")
                commandResultOutput = CommandResult("${config.name} is currently ${config.get()}.")
            else {
                try {
                    if (type == "remove") {
                        config.set(config.defaultValue)
                    } else if (config.isBool) {
                        config.set(BooleanParameter().parse(Boolean::class, value!!))
                    } else if (config.isNum) {
                        config.set(IntParameter().parse(Int::class, value!!))
                    } else if (config.isString) {
                        config.set(value!!.replace("\\n", "\n"))
                    }

                    commandResultOutput = CommandResult("${config.name} set to ${config.get()}.")
                } catch (error: CommandParameterValidationException) {
                    commandResultOutput = CommandResult(
                        error.message,
                        CommandResultStatus.Failed
                    )
                } catch (error: CommandParameterParsingException) {
                    commandResultOutput = CommandResult(error.toParametrizedString("value"), CommandResultStatus.Failed)
                }
            }

            Core.settings.forceSave()
        } else {
            commandResultOutput = CommandResult(
                "Unknown config: '$name'. Run the command with list parameter to get a list of valid configs.",
                CommandResultStatus.Failed
            )
        }

        return commandResultOutput
    }
}