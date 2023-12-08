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
import kennarddh.genesis.commands.parameters.annotations.numbers.Max
import kennarddh.genesis.commands.parameters.annotations.numbers.Min
import kennarddh.genesis.commands.parameters.annotations.numbers.validateMax
import kennarddh.genesis.commands.parameters.annotations.numbers.validateMin
import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException
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
import kennarddh.genesis.common.InvalidEscapedCharacterException
import kennarddh.genesis.common.InvalidStringParsingException
import kennarddh.genesis.common.StringParser
import kennarddh.genesis.common.UnterminatedStringException
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
    private val parameterValidator: MutableMap<KClass<*>, MutableMap<KClass<*>, CommandParameterValidator<*>>> =
        mutableMapOf()

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

        registerValidationAnnotation(
            Min::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateMin
        )
        registerValidationAnnotation(
            Max::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateMax
        )
    }

    fun <T : Any, V : Any> registerValidationAnnotation(
        annotation: KClass<T>,
        parametersType: List<KClass<out V>>,
        validator: CommandParameterValidator<V>
    ) {
        parametersType.forEach {
            if (!parameterValidator.contains(it)) parameterValidator[it] = mutableMapOf()

            parameterValidator[it]!![annotation] = validator
        }
    }

    fun registerParameterConverter(from: KClass<*>, parameterConverter: CommandParameterConverter<*>) {
        parameterConverters[from] = parameterConverter
    }

    fun registerHandler(handler: Handler) {
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

            val isClientSideOnly = sides.contains(CommandSide.Client) && !sides.contains(CommandSide.Server)

            if (isClientSideOnly && (functionParameters.isEmpty() || functionParameters[0].type != typeOf<Player>()))
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} is client only it must accept player as the first parameter")

            val parameters: MutableList<CommandParameter> = mutableListOf()

            val commandFunctionParameters = if (isClientSideOnly) functionParameters.drop(1) else functionParameters

            for (commandFunctionParameter in commandFunctionParameters) {
                val parameterTypeKClass = commandFunctionParameter.type.classifier

                if (parameterConverters.contains(parameterTypeKClass)) {
                    parameters.add(
                        CommandParameter(
                            parameterTypeKClass as KClass<*>,
                            commandFunctionParameter.name ?: "Unknown Parameter",
                            // TODO: Validate all validator is registered
                            commandFunctionParameter.annotations.toTypedArray()
                        )
                    )
                } else {
                    throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${commandFunctionParameter.name} parameter with type $parameterTypeKClass converter is not registered.")
                }
            }

            commands[name] = CommandData(name, sides, handler, function, parameters.toTypedArray())
        }
    }

    private fun getCommandFromCommandString(commandString: String): CommandData? {
        val splitted = commandString.split(" ")

        if (splitted.isEmpty()) return null

        return commands[splitted[0]]
    }

    private fun removeCommandNameFromCommandString(command: CommandData, commandString: String): String {
        if (command.name.length == commandString.length) return ""

        return commandString.substring(command.name.length + 1)
    }

    private fun parseServerCommand(commandString: String) {
        val command = getCommandFromCommandString(commandString)

        val result = if (command == null || !command.sides.contains(CommandSide.Server)) {
            CommandResult("Command $commandString not found.", CommandResultStatus.Failed)
        } else {
            invokeCommand(command, commandString) { parameters ->
                command.function.call(command.handler, *parameters) as CommandResult
            }
        }

        handleCommandHandlerResult(result, null)
    }

    private fun parseClientCommand(commandString: String, player: Player) {
        val command = getCommandFromCommandString(commandString)

        val result = if (command == null || !command.sides.contains(CommandSide.Client)) {
            CommandResult("Command $commandString not found.", CommandResultStatus.Failed)
        } else {
            invokeCommand(command, commandString) { parameters ->
                if (!command.sides.contains(CommandSide.Server))
                    command.function.call(command.handler, player, *parameters) as CommandResult
                else
                    command.function.call(command.handler, *parameters) as CommandResult
            }
        }

        handleCommandHandlerResult(result, player)
    }

    private fun invokeCommand(
        command: CommandData,
        commandString: String,
        invoke: (Array<Any>) -> CommandResult
    ): CommandResult {
        val commandStringWithoutCommandName = removeCommandNameFromCommandString(command, commandString)

        return try {
            val parameters = parseCommandParameters(command, commandStringWithoutCommandName)

            invoke(parameters)
        } catch (error: InvalidCommandParameterException) {
            CommandResult(
                error.message ?: "Unknown Invalid Command Parameter Exception Occurred",
                CommandResultStatus.Failed
            )
        } catch (error: UnterminatedStringException) {
            CommandResult(error.message ?: "Unknown Unterminated String Exception Occurred", CommandResultStatus.Failed)
        } catch (error: InvalidEscapedCharacterException) {
            CommandResult(error.message ?: "Unknown Escaped Character Exception Occurred", CommandResultStatus.Failed)
        } catch (error: InvalidStringParsingException) {
            CommandResult(error.message ?: "Unknown Escaped Character Exception Occurred", CommandResultStatus.Failed)
        } catch (error: CommandParameterConverterParsingException) {
            CommandResult(
                error.message ?: "Unknown Parameter Conversion Exception Occurred",
                CommandResultStatus.Failed
            )
        } catch (error: CommandParameterValidationException) {
            CommandResult(
                error.message ?: "Unknown Parameter Validation Exception Occurred",
                CommandResultStatus.Failed
            )
        } catch (error: Exception) {
            // TODO: Add proper logging
            error.printStackTrace()
            CommandResult("Unknown Error Occurred", CommandResultStatus.Failed)
        }
    }

    private fun parseCommandParameters(command: CommandData, commandStringWithoutCommandName: String): Array<Any> {
        val parameters: MutableList<Any> = mutableListOf()

        val parsedString = StringParser.parseToArray(commandStringWithoutCommandName)

        // TODO: Add Command Usage
        if (command.parametersType.size > parsedString.size) {
            throw InvalidCommandParameterException("Too few parameters supplied. Usage: soon")
        } else if (command.parametersType.size < parsedString.size) {
            throw InvalidCommandParameterException("Too much parameters supplied. Usage: soon")
        }

        for (i in 0..<command.parametersType.size) {
            val parameterAsString = parsedString[i]
            val parameter = command.parametersType[i]

            try {
                val output = parameterConverters[parameter.type]!!.parse(parameterAsString)

                parameter.validator.forEach {
                    val validator = parameterValidator[parameter.type]!![it.annotationClass]

                    @Suppress("UNCHECKED_CAST")
                    val isValid = (validator as CommandParameterValidator<Any>).invoke(it, output!!)

                    // TODO: Add better description by using @Description(":parameterName: must be greater than :value:") annotation on parameter validation annotation. :string: is replaced based on annotation property name
                    if (!isValid)
                        throw CommandParameterValidationException("Parameter validation for parameter ${parameter.name} failed.")
                }

                parameters.add(output!!)
            } catch (error: CommandParameterConverterParsingException) {
                throw CommandParameterConverterParsingException(error.toParametrizedString(parameter.name))
            }
        }

        return parameters.toTypedArray()
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