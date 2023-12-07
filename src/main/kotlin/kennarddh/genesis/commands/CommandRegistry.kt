package kennarddh.genesis.commands

import arc.util.Reflect
import kennarddh.genesis.commands.annotations.ClientSide
import kennarddh.genesis.commands.annotations.Command
import kennarddh.genesis.commands.annotations.ServerSide
import kennarddh.genesis.handlers.Handler
import mindustry.Vars
import mindustry.gen.Player
import mindustry.server.ServerControl

class CommandRegistry {
    private val handlers: MutableList<Handler> = mutableListOf()
    private val commands: MutableMap<String, CommandData> = mutableMapOf()

    fun init() {
        Reflect.set(ServerControl.instance, "handler", InterceptedCommandHandler("") { command, _ ->
            parseServerCommand(command)
        })

        Reflect.set(Vars.netServer, "clientCommands", InterceptedCommandHandler("/") { command, player ->
            parseClientCommand(command, player!!)
        })
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

        if (!commands.contains(commandString) || !command!!.sides.contains(CommandSide.Server)) {
            println("No command found with the name $commandString")
            return
        }

        command.method.invoke(command.handler)
    }

    private fun parseClientCommand(commandString: String, player: Player) {
        val command = commands[commandString]

        if (!commands.contains(commandString) || !command!!.sides.contains(CommandSide.Client)) {
            println("No command found with the name $commandString")
            return
        }

        if (command.method.parameterCount > 0 && !command.sides.contains(CommandSide.Server))
            command.method.invoke(command.handler, player)
        else
            command.method.invoke(command.handler)
    }
}