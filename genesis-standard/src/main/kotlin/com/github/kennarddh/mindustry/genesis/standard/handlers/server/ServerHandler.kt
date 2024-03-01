package com.github.kennarddh.mindustry.genesis.standard.handlers.server

import arc.Core
import arc.util.Reflect
import arc.util.Strings
import arc.util.Timer
import arc.util.serialization.JsonReader
import arc.util.serialization.JsonValue
import arc.util.serialization.JsonValue.ValueType
import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.commands.ArcCommand
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.ClientSide
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Command
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Description
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.ServerSide
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.exceptions.CommandParameterValidationException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.parameterValidationDescriptionAnnotationToString
import com.github.kennarddh.mindustry.genesis.core.commands.result.CommandResult
import com.github.kennarddh.mindustry.genesis.core.commands.result.CommandResultStatus
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.standard.Logger
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.BooleanParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer.IntParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.validations.numbers.GTE
import mindustry.Vars.*
import mindustry.core.GameState
import mindustry.core.Version
import mindustry.game.Gamemode
import mindustry.gen.Call
import mindustry.gen.Player
import mindustry.io.JsonIO
import mindustry.maps.Map
import mindustry.maps.MapException
import mindustry.net.Administration
import mindustry.server.ServerControl
import kotlin.math.ceil
import kotlin.math.min
import kotlin.reflect.full.findAnnotation


class ServerHandler : Handler {
    override suspend fun onInit() {
        Genesis.commandRegistry.removeCommand("help")
        Genesis.commandRegistry.removeCommand("host")
        Genesis.commandRegistry.removeCommand("version")
        Genesis.commandRegistry.removeCommand("maps")
        Genesis.commandRegistry.removeCommand("mods")
        Genesis.commandRegistry.removeCommand("mod")
        Genesis.commandRegistry.removeCommand("js")
        Genesis.commandRegistry.removeCommand("pause")
        Genesis.commandRegistry.removeCommand("rules")
        Genesis.commandRegistry.removeCommand("config")
        Genesis.commandRegistry.removeCommand("playerlimit")
    }

    @Command(["help"])
    @ServerSide
    @ClientSide
    @Description("Display the command list, or get help for a specific command.")
    suspend fun help(player: Player? = null, commandOrPage: String = "1"): CommandResult {
        val commandsPerPage = 10

        var page: Int? = null
        var commandName: String? = null

        try {
            page = IntParameter().parse(Int::class, commandOrPage)
        } catch (e: CommandParameterParsingException) {
            commandName = commandOrPage
        }

        val isServer = player == null

        val commands = if (isServer)
            Genesis.commandRegistry.serverCommands.toList()
        else
            Genesis.commandRegistry.clientCommands.toList()

        val output = buildString {
            if (page != null) {
                appendLine("Commands:")

                if (page <= 0)
                    return CommandResult("Parameter page must be greater than 0", CommandResultStatus.Failed)

                val maxPage = ceil(commands.size.toDouble() / commandsPerPage).toInt()

                if (page > maxPage)
                    return CommandResult(
                        "Help max page is $maxPage",
                        CommandResultStatus.Failed
                    )

                commands.subList((page - 1) * commandsPerPage, min(page * commandsPerPage, commands.size)).forEach {
                    // Ignore alias command
                    if (it is ArcCommand && it.isAlias) return@forEach

                    val name = if (it is ArcCommand) it.realName else it.text
                    // TODO: Get usages concurrently with async
                    val usage = if (it is ArcCommand) it.toUsage() else it.paramText
                    val brief = if (it is ArcCommand) it.brief else it.description

                    append("\t")

                    if (!isServer)
                        append("[orange]")

                    append(
                        if (isServer)
                            Genesis.commandRegistry.serverPrefix
                        else
                            Genesis.commandRegistry.clientPrefix
                    )

                    append(name)

                    if (!isServer)
                        append("[lightgray]")

                    append(' ')
                    append(usage)

                    if (brief.isNotEmpty()) {
                        if (usage.isNotEmpty())
                            append(' ')

                        if (!isServer)
                            append("[gray]")

                        append("- ")

                        append(brief)
                    }

                    append('\n')
                }
            } else {
                val command = commands.find { if (it is ArcCommand) it.name == commandName else it.text == commandName }
                    ?: return CommandResult("Command $commandName not found.", CommandResultStatus.Failed)

                val name = if (command is ArcCommand) command.realName else command.text
                val usage = if (command is ArcCommand) command.toUsage() else command.paramText
                val brief = if (command is ArcCommand) command.brief else command.description
                val description = command.description

                appendLine("Command $name:")

                append("\t")

                if (!isServer)
                    append("[orange]")

                append(
                    if (isServer)
                        Genesis.commandRegistry.serverPrefix
                    else
                        Genesis.commandRegistry.clientPrefix
                )

                append(name)

                if (!isServer)
                    append("[lightgray]")

                append(' ')
                append(usage)

                if (brief.isNotEmpty()) {
                    if (usage.isNotEmpty())
                        append(' ')

                    if (!isServer)
                        append("[gray]")

                    append("- ")

                    append(brief)
                }

                if (description.isNotEmpty() && brief != description) {
                    append('\n')
                    append(description)
                }

                append('\n')

                if (command is ArcCommand) {
                    command.commandData.parametersType.forEach {
                        it.validator.forEach { validator ->
                            val validatorDescriptionAnnotation =
                                validator.annotationClass.findAnnotation<ParameterValidationDescription>()

                            if (validatorDescriptionAnnotation != null) {
                                append("\t- ")
                                appendLine(
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
            }
        }

        return CommandResult(output.trimEnd('\n'))
    }

    @Command(["host"])
    @ServerSide
    @Description("Open the server. Will default to survival and a random map if not specified.")
    fun host(mapName: String? = null, gameMode: Gamemode = Gamemode.survival): CommandResult {
        if (state.isGame)
            return CommandResult("Already hosting. Type 'stop' to stop hosting first.", CommandResultStatus.Failed)

        runOnMindustryThread {
            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()
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
            map = maps.shuffleMode.next(gameMode, state.map)

            Logger.info("Randomized next map to be ${map.plainName()}.")
        }

        Logger.info("Loading map...")

        runOnMindustryThread {
            logic.reset()

            ServerControl.instance.lastMode = gameMode

            Core.settings.put("lastServerMode", ServerControl.instance.lastMode.name)
        }

        try {
            runOnMindustryThread {
                world.loadMap(map, map.applyRules(ServerControl.instance.lastMode))
                state.rules = map.applyRules(gameMode)
                logic.play()

                Logger.info("Map loaded.")

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
            Java Version:
                ${System.getProperty("java.vendor")}
                Version: "${System.getProperty("java.version")}" ${System.getProperty("java.version.date")}
                ${System.getProperty("java.runtime.name")} (build ${System.getProperty("java.runtime.version")})
                ${System.getProperty("java.vm.name")} (build ${System.getProperty("java.runtime.version")}, ${
                System.getProperty(
                    "java.vm.info"
                )
            })
            Operating System:
                Arch: ${System.getProperty("os.arch")}
                Name: ${System.getProperty("os.name")}
                Version: ${System.getProperty("os.version")}
            """.trimIndent()
        )
    }

    enum class MapsCommandType {
        custom, default, all, get
    }

    @Command(["maps"])
    @ServerSide
    @Description("Display available maps. Displays only custom maps by default.")
    fun maps(type: MapsCommandType = MapsCommandType.custom): CommandResult {
        var showCustom = false
        var showDefault = false

        when (type) {
            MapsCommandType.custom -> showCustom = true
            MapsCommandType.default -> showDefault = true
            MapsCommandType.all -> {
                showCustom = true
                showDefault = true
            }

            else -> return CommandResult(
                "$type is an invalid type. Possible value are custom, default, all.",
                CommandResultStatus.Failed
            )
        }

        val output = buildString {
            if (!maps.all().isEmpty) {
                val all: MutableList<Map> = mutableListOf()

                if (showCustom) all.addAll(maps.customMaps())
                if (showDefault) all.addAll(maps.defaultMaps())

                if (all.isEmpty() && !showDefault) {
                    appendLine("No custom maps loaded. Set default as the first parameter to show default maps.")
                } else {
                    appendLine("Maps:")

                    for (map in all) {
                        val mapName = map.plainName().replace(' ', '_')

                        if (map.custom) {
                            appendLine("\t${mapName} (${map.file.name()}): Custom / ${map.width}x${map.height}")
                        } else {
                            appendLine("\t${mapName}: Default / ${map.width}x${map.height}")
                        }
                    }
                }
            } else {
                appendLine("No maps found.")
            }

            appendLine("Map directory: ${customMapDirectory.file().getAbsoluteFile()}")

            trimEnd('\n')
        }

        return CommandResult(output)
    }

    @Command(["mods", "plugins"])
    @ServerSide
    @Description("Display all loaded mods/plugins.")
    fun mods(): CommandResult {
        val output = buildString {

            if (!mods.list().isEmpty) {
                appendLine("Mods:")

                for (mod in mods.list()) {
                    appendLine("\t${mod.meta.displayName}: ${mod.meta.name}@${mod.meta.version} ${if (mod.enabled()) "" else " (${mod.state})"}")
                }
            } else {
                appendLine("No mods found.")
            }

            appendLine("Mod directory: ${modDirectory.file().getAbsoluteFile()}")

            trimEnd('\n')
        }

        return CommandResult(output)
    }

    @Command(["mod", "plugin"])
    @ServerSide
    @Description("Display information about a loaded mod/plugin.")
    fun mod(name: String): CommandResult {
        val output = buildString {
            val mod = mods.list().find { it.meta.name.equals(name, ignoreCase = true) }

            if (mod != null) {
                appendLine("Name: ${mod.meta.displayName}")
                appendLine("Internal Name: ${mod.name}")
                appendLine("Version: ${mod.meta.version}")
                appendLine("Author: ${mod.meta.author}")
                appendLine("Path: ${mod.file.path()}")
                appendLine("Description: ${mod.meta.description}")
            } else {
                appendLine("No mod with name $name found.")
            }

            trimEnd('\n')
        }

        return CommandResult(output)
    }

    @Command(["javascript", "js"])
    @ServerSide
    @Description("Run arbitrary Javascript.")
    fun javascript(script: String): CommandResult {
        return CommandResult(mods.scripts.runConsole(script))
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

    enum class RulesCommandType {
        list, add, remove
    }

    @Command(["rules"])
    @ServerSide
    @Description("List, remove or add global rules. These will apply regardless of map.")
    fun rules(
        type: RulesCommandType = RulesCommandType.list,
        name: String? = null,
        value: String? = null
    ): CommandResult {
        when (type) {
            RulesCommandType.list -> {
                if (name != null) return CommandResult(
                    "Name is not required for list",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for list",
                    CommandResultStatus.Failed
                )
            }

            RulesCommandType.add -> {
                if (name == null) return CommandResult(
                    "Name is required for add",
                    CommandResultStatus.Failed
                )

                if (value == null) return CommandResult(
                    "Value is required for add",
                    CommandResultStatus.Failed
                )
            }

            RulesCommandType.remove -> {
                if (name == null) return CommandResult(
                    "Name is required for remove",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for remove",
                    CommandResultStatus.Failed
                )
            }
        }

        var commandResultOutput: CommandResult

        val rules = Core.settings.getString("globalrules")
        val base = JsonIO.json.fromJson<JsonValue>(null, rules)

        if (type == RulesCommandType.list)
            commandResultOutput = CommandResult("Rules:\n${JsonIO.print(rules)}")
        else {
            if (type == RulesCommandType.remove) {
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

    enum class ConfigCommandType {
        list, add, remove, get
    }

    @Command(["config"])
    @ServerSide
    @Description("Configure server settings.")
    suspend fun config(
        type: ConfigCommandType = ConfigCommandType.list,
        name: String? = null,
        value: String? = null
    ): CommandResult {
        when (type) {
            ConfigCommandType.list -> {
                if (name != null) return CommandResult(
                    "Name is not required for list",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for list",
                    CommandResultStatus.Failed
                )
            }

            ConfigCommandType.add -> {
                if (name == null) return CommandResult(
                    "Name is required for add",
                    CommandResultStatus.Failed
                )

                if (value == null) return CommandResult(
                    "Value is required for add",
                    CommandResultStatus.Failed
                )
            }

            ConfigCommandType.remove, ConfigCommandType.get -> {
                if (name == null) return CommandResult(
                    "Name is required for $type",
                    CommandResultStatus.Failed
                )

                if (value != null) return CommandResult(
                    "Value is not required for $type",
                    CommandResultStatus.Failed
                )
            }
        }

        var commandResultOutput: CommandResult

        if (type == ConfigCommandType.list) {
            val output = buildString {
                appendLine("All config values:")

                for (config in Administration.Config.all) {
                    appendLine("\t| ${config.name}: ${config.get()}")
                    appendLine("\t| | ${config.description}")
                }

                trimEnd('\n')
            }

            return CommandResult(output)
        }

        val config = Administration.Config.all.find { it.name.equals(name, ignoreCase = true) }

        if (config != null) {
            if (type == ConfigCommandType.get)
                commandResultOutput = CommandResult("${config.name} is currently ${config.get()}.")
            else {
                try {
                    if (type == ConfigCommandType.remove) {
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