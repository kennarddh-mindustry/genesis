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
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Command
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Description
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.exceptions.CommandParameterValidationException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.parameterValidationDescriptionAnnotationToString
import com.github.kennarddh.mindustry.genesis.core.commands.senders.CommandSender
import com.github.kennarddh.mindustry.genesis.core.commands.senders.ServerCommandSender
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThread
import com.github.kennarddh.mindustry.genesis.core.commons.runOnMindustryThreadSuspended
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
    @Description("Display the command list, or get help for a specific command.")
    suspend fun help(sender: CommandSender, commandOrPage: String = "1") {
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
                    return sender.sendError("Parameter page must be greater than 0")

                val maxPage = ceil(commands.size.toDouble() / commandsPerPage).toInt()

                if (page > maxPage)
                    return sender.sendError("Help max page is $maxPage")

                commands.subList((page - 1) * commandsPerPage, min(page * commandsPerPage, commands.size)).forEach {
                    // Ignore alias command
                    if (it is ArcCommand && it.isAlias) return@forEach

                    val name = if (it is ArcCommand) it.realName else it.text

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
                    ?: return sender.sendError("Command $commandName not found.")

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

        sender.sendSuccess(output.trimEnd('\n'))
    }

    @Command(["host"])
    @Description("Open the server. Will default to survival and a random map if not specified.")
    fun host(
        sender: ServerCommandSender,
        mapName: String? = null,
        gameMode: Gamemode = Gamemode.survival
    ) {
        runOnMindustryThread {
            if (state.isGame)
                return@runOnMindustryThread sender.sendError("Already hosting. Type 'stop' to stop hosting first.")

            // TODO: When v147 released replace this with ServerControl.instance.cancelPlayTask()
            Reflect.get<Timer.Task>(ServerControl.instance, "lastTask")?.cancel()

            val map: Map?
            if (mapName != null) {
                map = maps.all().find {
                    it.plainName().replace('_', ' ')
                        .equals(Strings.stripColors(mapName).replace('_', ' '), ignoreCase = true)
                }

                if (map == null)
                    return@runOnMindustryThread sender.sendError("Map with name $mapName not found.")
            } else {
                map = maps.shuffleMode.next(gameMode, state.map)

                Logger.info("Randomized next map to be ${map.plainName()}.")
            }

            Logger.info("Loading map...")

            logic.reset()

            ServerControl.instance.lastMode = gameMode

            Core.settings.put("lastServerMode", ServerControl.instance.lastMode.name)

            try {
                world.loadMap(map, map.applyRules(ServerControl.instance.lastMode))
                state.rules = map.applyRules(gameMode)
                logic.play()

                Logger.info("Map loaded.")

                netServer.openServer()

                if (Administration.Config.autoPause.bool()) {
                    state.set(GameState.State.paused)

                    Reflect.set(ServerControl.instance, "autoPaused", true)
                }

                sender.sendSuccess("Host success")
            } catch (e: MapException) {
                sender.sendError("${e.map.plainName()}: ${e.message}")
            }
        }
    }

    @Command(["version"])
    @Description("Displays server version info.")
    fun version(sender: ServerCommandSender) {
        sender.sendSuccess(
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
    @Description("Display available maps. Displays only custom maps by default.")
    fun maps(sender: ServerCommandSender, type: MapsCommandType = MapsCommandType.custom) {
        var showCustom = false
        var showDefault = false

        when (type) {
            MapsCommandType.custom -> showCustom = true
            MapsCommandType.default -> showDefault = true
            MapsCommandType.all -> {
                showCustom = true
                showDefault = true
            }

            else -> return sender.sendError(
                "$type is an invalid type. Possible value are custom, default, all.",
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

        sender.sendSuccess(output)
    }

    @Command(["mods", "plugins"])
    @Description("Display all loaded mods/plugins.")
    fun mods(sender: ServerCommandSender) {
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

        sender.sendSuccess(output)
    }

    @Command(["mod", "plugin"])
    @Description("Display information about a loaded mod/plugin.")
    fun mod(sender: ServerCommandSender, name: String) {
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

        sender.sendSuccess(output)
    }

    @Command(["javascript", "js"])
    @Description("Run arbitrary Javascript.")
    fun javascript(sender: ServerCommandSender, script: String) {
        runOnMindustryThread {
            sender.sendMessage(mods.scripts.runConsole(script))
        }
    }

    @Command(["pause"])
    @Description("Pause or unpause the game.")
    fun pause(sender: ServerCommandSender, pause: Boolean) {
        runOnMindustryThread {
            if (state.isMenu)
                return@runOnMindustryThread sender.sendError("Cannot pause without a game running.")

            Reflect.set(ServerControl.instance, "autoPaused", false)

            state.set(if (state.isPaused) GameState.State.playing else GameState.State.paused)

            sender.sendSuccess(if (pause) "Game paused." else "Game unpaused.")
        }
    }

    enum class RulesCommandType {
        list, add, remove
    }

    @Command(["rules"])
    @Description("List, remove or add global rules. These will apply regardless of map.")
    fun rules(
        sender: ServerCommandSender,
        type: RulesCommandType = RulesCommandType.list,
        name: String? = null,
        value: String? = null
    ) {
        when (type) {
            RulesCommandType.list -> {
                if (name != null) return sender.sendError(
                    "Name is not required for list",
                )

                if (value != null) return sender.sendError(
                    "Value is not required for list"
                )
            }

            RulesCommandType.add -> {
                if (name == null) return sender.sendError(
                    "Name is required for add",
                )

                if (value == null) return sender.sendError(
                    "Value is required for add",
                )
            }

            RulesCommandType.remove -> {
                if (name == null) return sender.sendError(
                    "Name is required for remove",
                )

                if (value != null) return sender.sendError(
                    "Value is not required for remove",
                )
            }
        }

        runOnMindustryThread {
            val rules = Core.settings.getString("globalrules")
            val base = JsonIO.json.fromJson<JsonValue>(null, rules)

            if (type == RulesCommandType.list) {
                sender.sendSuccess("Rules:\n${JsonIO.print(rules)}")
            } else if (type == RulesCommandType.remove) {
                if (base.has(name)) {
                    base.remove(name)

                    sender.sendSuccess("Rule '${name}' removed.")
                } else {
                    sender.sendError("Rule not defined, so not removed.")
                }
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

                    sender.sendSuccess("Changed rule: ${jsonValue.toString().replace("\n", " ")}")
                } catch (e: Throwable) {

                    sender.sendError("Error parsing rule JSON: ${e.message}")
                }
            }

            Core.settings.put("globalrules", base.toString())

            Call.setRules(state.rules)
        }
    }

    @Command(["player_limit", "playerlimit"])
    @Description("Set the server player limit. 0 to disable limit")
    suspend fun playerLimit(sender: ServerCommandSender, @GTE(0) limit: Int? = null) {
        if (limit == null)
            return sender.sendError(
                "Player limit is currently ${netServer.admins.playerLimit}.",
            )

        runOnMindustryThreadSuspended {
            netServer.admins.playerLimit = limit
        }

        if (limit == 0)
            sender.sendSuccess("Player limit disabled.")
        else
            sender.sendSuccess("Player limit is now $limit")
    }

    enum class ConfigCommandType {
        list, add, remove, get
    }

    @Command(["config"])
    @Description("Configure server settings.")
    suspend fun config(
        sender: ServerCommandSender,
        type: ConfigCommandType = ConfigCommandType.list,
        name: String? = null,
        value: String? = null
    ) {
        when (type) {
            ConfigCommandType.list -> {
                if (name != null) return sender.sendError(
                    "Name is not required for list"
                )

                if (value != null) return sender.sendError(
                    "Value is not required for list"
                )
            }

            ConfigCommandType.add -> {
                if (name == null) return sender.sendError(
                    "Name is required for add",
                )

                if (value == null) return sender.sendError(
                    "Value is required for add",
                )
            }

            ConfigCommandType.remove, ConfigCommandType.get -> {
                if (name == null) return sender.sendError(
                    "Name is required for $type",
                )

                if (value != null) return sender.sendError(
                    "Value is not required for $type",
                )
            }
        }

        if (type == ConfigCommandType.list) {
            val output = buildString {
                appendLine("All config values:")

                for (config in Administration.Config.all) {
                    appendLine("\t| ${config.name}: ${config.get()}")
                    appendLine("\t| | ${config.description}")
                }

                trimEnd('\n')
            }

            return sender.sendSuccess(output)
        }

        val config = Administration.Config.all.find { it.name.equals(name, ignoreCase = true) }

        if (config == null)
            sender.sendError(
                "Unknown config: '$name'. Run the command with list parameter to get a list of valid configs."
            )

        if (type == ConfigCommandType.get) {
            return sender.sendSuccess("${config.name} is currently ${config.get()}.")
        }

        try {
            if (type == ConfigCommandType.remove) {
                runOnMindustryThreadSuspended {
                    config.set(config.defaultValue)
                }
            } else if (config.isBool) {
                val boolValue = BooleanParameter().parse(Boolean::class, value!!)

                runOnMindustryThreadSuspended {
                    config.set(boolValue)
                }
            } else if (config.isNum) {
                val intValue = IntParameter().parse(Int::class, value!!)

                runOnMindustryThreadSuspended {
                    config.set(intValue)
                }
            } else if (config.isString) {
                runOnMindustryThreadSuspended {
                    config.set(value!!.replace("\\n", "\n"))
                }
            }

            runOnMindustryThreadSuspended {
                Core.settings.forceSave()
            }

            sender.sendSuccess("${config.name} set to ${config.get()}.")
        } catch (error: CommandParameterValidationException) {
            sender.sendError(error.message)
        } catch (error: CommandParameterParsingException) {
            sender.sendError(error.toParametrizedString("value"))
        }
    }
}