package kennarddh.genesis.commands

import arc.Core
import arc.util.Log
import arc.util.Reflect
import kennarddh.genesis.commands.annotations.ClientSide
import kennarddh.genesis.commands.annotations.Command
import kennarddh.genesis.commands.annotations.ServerSide
import kennarddh.genesis.commands.parameters.BooleanParameterConverter
import kennarddh.genesis.commands.parameters.CharParameterConverter
import kennarddh.genesis.commands.parameters.StringParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.numbers.signed.floating.DoubleParameterConverter
import kennarddh.genesis.commands.parameters.numbers.signed.floating.FloatParameterConverter
import kennarddh.genesis.commands.parameters.numbers.signed.integer.ByteParameterConverter
import kennarddh.genesis.commands.parameters.numbers.signed.integer.IntParameterConverter
import kennarddh.genesis.commands.parameters.numbers.signed.integer.LongParameterConverter
import kennarddh.genesis.commands.parameters.numbers.signed.integer.ShortParameterConverter
import kennarddh.genesis.commands.parameters.numbers.unsigned.integer.UByteParameterConverter
import kennarddh.genesis.commands.parameters.numbers.unsigned.integer.UIntParameterConverter
import kennarddh.genesis.commands.parameters.numbers.unsigned.integer.ULongParameterConverter
import kennarddh.genesis.commands.parameters.numbers.unsigned.integer.UShortParameterConverter
import kennarddh.genesis.commands.result.CommandResult
import kennarddh.genesis.commands.result.CommandResultStatus
import kennarddh.genesis.handlers.Handler
import mindustry.Vars
import mindustry.gen.Player
import mindustry.server.ServerControl
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

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

        registerParameterConverter(Boolean::class, BooleanParameterConverter())
        registerParameterConverter(Char::class, CharParameterConverter())
        registerParameterConverter(String::class, StringParameterConverter())

        registerParameterConverter(Float::class, FloatParameterConverter())
        registerParameterConverter(Double::class, DoubleParameterConverter())

        registerParameterConverter(Byte::class, ByteParameterConverter())
        registerParameterConverter(Short::class, ShortParameterConverter())
        registerParameterConverter(Int::class, IntParameterConverter())
        registerParameterConverter(Long::class, LongParameterConverter())

        registerParameterConverter(UByte::class, UByteParameterConverter())
        registerParameterConverter(UShort::class, UShortParameterConverter())
        registerParameterConverter(UInt::class, UIntParameterConverter())
        registerParameterConverter(ULong::class, ULongParameterConverter())
    }

    fun registerParameterConverter(from: KClass<*>, parameterConverter: CommandParameterConverter<*>) {
        parameterConverters[from] = parameterConverter
    }

    fun addHandler(handler: Handler) {
        handlers.add(handler)

        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val commandAnnotation = function.findAnnotation<Command>() ?: continue

            val clientSideAnnotation = function.findAnnotation<ClientSide>()
            val serverSideAnnotation = function.findAnnotation<ServerSide>()

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
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} need to have either ServerSide or ClientSide or both annotation")
            }

            val functionParameters = function.parameters.drop(1)

            val parameters: MutableList<KClass<*>> = mutableListOf()

            val isClientSideOnly = sides.contains(CommandSide.Client) && !sides.contains(CommandSide.Server)

            if (isClientSideOnly && (functionParameters.isEmpty() || functionParameters[0].type != typeOf<Player>()))
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} is client only it must accept player as the first parameter")

            commands[name] = CommandData(sides, handler, function, parameters)
        }
    }

    private fun parseServerCommand(commandString: String) {
        val command = commands[commandString]

        val result = if (!commands.contains(commandString) || !command!!.sides.contains(CommandSide.Server)) {
            CommandResult("Command $commandString not found.", CommandResultStatus.Failed)
        } else {
            command.function.call(command.handler)
        }

        handleCommandHandlerResult(result, null)
    }

    private fun parseClientCommand(commandString: String, player: Player) {
        val command = commands[commandString]

        val result = if (!commands.contains(commandString) || !command!!.sides.contains(CommandSide.Client)) {
            CommandResult("Command $commandString not found.", CommandResultStatus.Failed)
        } else {
            if (!command.sides.contains(CommandSide.Server))
                command.function.call(command.handler, player)
            else
                command.function.call(command.handler)
        }

        handleCommandHandlerResult(result, player)
    }

    private fun handleCommandHandlerResult(result: Any?, player: Player?) {
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