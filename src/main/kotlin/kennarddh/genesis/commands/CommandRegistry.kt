package kennarddh.genesis.commands

import arc.Core
import arc.util.Log
import arc.util.Reflect
import kennarddh.genesis.commands.annotations.ClientSide
import kennarddh.genesis.commands.annotations.Command
import kennarddh.genesis.commands.annotations.ServerSide
import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.result.CommandResult
import kennarddh.genesis.commands.result.CommandResultStatus
import kennarddh.genesis.handlers.Handler
import mindustry.Vars
import mindustry.gen.Player
import mindustry.server.ServerControl
import kotlin.reflect.KClass

class CommandRegistry {
    private val handlers: MutableList<Handler> = mutableListOf()
    private val commands: MutableMap<String, CommandData> = mutableMapOf()

    private val parameterConverters: MutableMap<KClass<*>, CommandParameterConverter<*>> = mutableMapOf()

    fun init() {
        Reflect.set(ServerControl.instance, "handler", InterceptedCommandHandler("") { command, _ ->
            parseServerCommand(command)
        })

        Reflect.set(Vars.netServer, "clientCommands", InterceptedCommandHandler("/") { command, player ->
            parseClientCommand(command, player!!)
        })
    }

    fun registerParameterConverter(from: KClass<*>, parameterConverter: CommandParameterConverter<*>) {
        parameterConverters[from] = parameterConverter
    }

    fun addHandler(handler: Handler) {
        handlers.add(handler)

        for (method in handler::class.java.declaredMethods) {
            method.isAccessible = true

            val commandAnnotation = method.getAnnotation(Command::class.java) ?: continue

            val clientSideAnnotation = method.getAnnotation(ClientSide::class.java)
            val serverSideAnnotation = method.getAnnotation(ServerSide::class.java)

            val name = commandAnnotation.name

            val isServerSide = serverSideAnnotation != null
            val isClientSide = clientSideAnnotation != null

            val sides: Array<CommandSide> = if (isServerSide && isClientSide) {
                arrayOf(CommandSide.Server, CommandSide.Client)
            } else if (isServerSide) {
                arrayOf(CommandSide.Server)
            } else if (isClientSide) {
                arrayOf(CommandSide.Client)
            } else {
                throw Exception("Command need to have either ServerSide or ClientSide or both annotation")
            }

            commands[name] = CommandData(sides, handler, method)
        }
    }

    private fun parseServerCommand(commandString: String) {
        val command = commands[commandString]

        val result = if (!commands.contains(commandString) || !command!!.sides.contains(CommandSide.Server)) {
            CommandResult("Command $commandString not found.", CommandResultStatus.Failed)
        } else {
            command.method.invoke(command.handler)
        }

        handleCommandHandlerResult(result, null)
    }

    private fun parseClientCommand(commandString: String, player: Player) {
        val command = commands[commandString]

        val result = if (!commands.contains(commandString) || !command!!.sides.contains(CommandSide.Client)) {
            CommandResult("Command $commandString not found.", CommandResultStatus.Failed)
        } else {
            if (command.method.parameterCount > 0 && !command.sides.contains(CommandSide.Server))
                command.method.invoke(command.handler, player)
            else
                command.method.invoke(command.handler)
        }

        handleCommandHandlerResult(result, player)
    }

    private fun handleCommandHandlerResult(result: Any, player: Player?) {
        if (result !is CommandResult) return

        if (result.status == CommandResultStatus.Empty) return

        if (player != null) {
            val colorString = if (result.colorDependsOnStatus)
                when (result.status) {
                    CommandResultStatus.Failed -> "[#ff0000]"
                    CommandResultStatus.Success -> "[#00ff00]"
                    else -> {
                        Log.warn("Unknown CommandResultStatus ${result.status}")
                        "[#ffffff]"
                    }
                } else
                ""

            Core.app.post {
                player.sendMessage("${colorString}${result.response}")
            }
        } else {
            when (result.status) {
                CommandResultStatus.Failed -> Log.err(result.response)
                CommandResultStatus.Success -> Log.info(result.response)
                else -> Log.warn("Unknown CommandResultStatus ${result.status}: ${result.response}")
            }
        }
    }
}