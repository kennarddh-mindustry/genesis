package kennarddh.genesis.core.commands

import arc.Core
import arc.util.Log
import arc.util.Reflect
import kennarddh.genesis.core.commands.annotations.*
import kennarddh.genesis.core.commands.exceptions.DuplicateCommandNameException
import kennarddh.genesis.core.commands.exceptions.InvalidCommandMethodException
import kennarddh.genesis.core.commands.parameters.CommandParameter
import kennarddh.genesis.core.commands.parameters.CommandParameterValidator
import kennarddh.genesis.core.commands.parameters.converters.BooleanParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.CharParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.StringParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverterParsingException
import kennarddh.genesis.core.commands.parameters.converters.numbers.signed.floating.DoubleParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.signed.floating.FloatParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.signed.integer.ByteParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.signed.integer.IntParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.signed.integer.LongParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.signed.integer.ShortParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.unsigned.integer.UByteParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.unsigned.integer.UIntParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.unsigned.integer.ULongParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.numbers.unsigned.integer.UShortParameterConverter
import kennarddh.genesis.core.commands.parameters.exceptions.CommandParameterValidationException
import kennarddh.genesis.core.commands.parameters.exceptions.InvalidCommandParameterException
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidation
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidationDescription
import kennarddh.genesis.core.commands.parameters.validations.numbers.*
import kennarddh.genesis.core.commands.parameters.validations.parameterValidationDescriptionAnnotationToString
import kennarddh.genesis.core.commands.result.CommandResult
import kennarddh.genesis.core.commands.result.CommandResultStatus
import kennarddh.genesis.core.common.*
import kennarddh.genesis.core.handlers.Handler
import mindustry.Vars
import mindustry.gen.Player
import mindustry.server.ServerControl
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

class CommandRegistry {
    private val commands: MutableList<CommandData> = mutableListOf()

    private val parameterConverters: MutableMap<KClass<*>, CommandParameterConverter<*>> = mutableMapOf()
    private val parameterValidator: MutableMap<KClass<*>, MutableMap<KClass<*>, CommandParameterValidator<*>>> =
        mutableMapOf()

    private val serverInterceptedCommandHandler = InterceptedCommandHandler("") { command, _ ->
        parseServerCommand(command)
    }

    private val clientInterceptedCommandHandler = InterceptedCommandHandler("/") { command, player ->
        parseClientCommand(command, player!!)
    }

    @Suppress("UNUSED")
    var clientPrefix: String
        get() = clientInterceptedCommandHandler.getPrefix()
        set(newPrefix) = clientInterceptedCommandHandler.setPrefix(newPrefix)

    @Suppress("UNUSED")
    var serverPrefix: String
        get() = serverInterceptedCommandHandler.getPrefix()
        set(newPrefix) = serverInterceptedCommandHandler.setPrefix(newPrefix)

    fun init() {
        Reflect.set(ServerControl.instance, "handler", serverInterceptedCommandHandler)

        Reflect.set(Vars.netServer, "clientCommands", clientInterceptedCommandHandler)

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
            GT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGT
        )
        registerValidationAnnotation(
            GTE::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGTE
        )
        registerValidationAnnotation(
            LT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateLT
        )
        registerValidationAnnotation(
            LTE::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateLTE
        )
    }

    companion object {
        fun commandToUsage(command: CommandData): String {
            TODO("Usage")
        }
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
        // TODO: Check if parameter type already registered

        parameterConverters[from] = parameterConverter
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val commandAnnotation = function.findAnnotation<Command>() ?: continue

            val clientSideAnnotation = function.findAnnotation<ClientSide>()
            val serverSideAnnotation = function.findAnnotation<ServerSide>()

            val descriptionAnnotation = function.findAnnotation<Description>()
            val briefAnnotation = function.findAnnotation<Brief>()

            val names = commandAnnotation.names

            val description = descriptionAnnotation?.description ?: ""
            val brief = briefAnnotation?.brief ?: description

            val checkedNames: MutableList<String> = mutableListOf()

            for (name in names) {
                if (checkedNames.contains(name))
                    throw DuplicateCommandNameException("Method ${handler::class.qualifiedName}.${function.name} register $name command multiple times")

                val command = getCommandFromCommandName(name)

                if (command != null)
                    throw DuplicateCommandNameException("Command $name for method ${handler::class.qualifiedName}.${function.name} has already been registered")

                checkedNames.add(name)
            }

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

            if (isClientSide && !isServerSide && functionParameters.isNotEmpty() && functionParameters[0].type == typeOf<Player?>())
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} support client only must accept non optional player as the first parameter")

            if (isClientSide && isServerSide && functionParameters.isNotEmpty() && (!functionParameters[0].isOptional || functionParameters[0].type != typeOf<Player?>()))
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} support client and server must accept optional player as the first parameter")

            val parameters: MutableList<CommandParameter> = mutableListOf()

            functionParameters.forEachIndexed { index, functionParameter ->
                if (isClientSide && index == 0) {
                    parameters.add(
                        CommandParameter(
                            functionParameter,
                            arrayOf(),
                        )
                    )

                    return@forEachIndexed
                }

                val parameterTypeKClass = functionParameter.type.classifier

                if (!parameterConverters.contains(parameterTypeKClass))
                    throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${functionParameter.name} parameter with type $parameterTypeKClass converter is not registered.")

                val validationsAnnotation: List<Annotation> =
                    functionParameter.annotations.filter { it.annotationClass.hasAnnotation<ParameterValidation>() }

                parameters.add(
                    CommandParameter(
                        functionParameter,
                        validationsAnnotation.toTypedArray(),
                    )
                )
            }

            commands.add(CommandData(names, description, brief, sides, handler, function, parameters.toTypedArray()))
        }
    }

    private fun getCommandFromCommandName(commandName: String?): CommandData? {
        for (command in commands) {
            if (command.names.contains(commandName)) return command
        }

        return null
    }

    private fun getCommandNameFromCommandString(commandString: String): String? {
        val splitted = commandString.split(" ")

        if (splitted.isEmpty()) return null

        return splitted[0]
    }

    private fun removeCommandNameFromCommandString(commandName: String, commandString: String): String {
        if (commandName.length == commandString.length) return ""

        return commandString.substring(commandName.length + 1)
    }

    private fun parseServerCommand(commandString: String) {
        val commandName = getCommandNameFromCommandString(commandString)
        val command = getCommandFromCommandName(commandName)

        val result = if (command == null || !command.sides.contains(CommandSide.Server)) {
            CommandResult("Command $commandName not found.", CommandResultStatus.Failed)
        } else {
            invokeCommand(commandName!!, command, commandString, null) { parameters ->
                command.function.callBy(parameters)
            }
        }

        handleCommandHandlerResult(result, null)
    }

    private fun parseClientCommand(commandString: String, player: Player) {
        val commandName = getCommandNameFromCommandString(commandString)
        val command = getCommandFromCommandName(commandName)

        val result = if (command == null || !command.sides.contains(CommandSide.Client)) {
            CommandResult("Command $commandName not found.", CommandResultStatus.Failed)
        } else {
            invokeCommand(commandName!!, command, commandString, player) { parameters ->
                command.function.callBy(parameters)
            }
        }

        handleCommandHandlerResult(result, player)
    }

    private fun invokeCommand(
        invokedCommandName: String,
        command: CommandData,
        commandString: String,
        player: Player?,
        invoke: (Map<KParameter, Any?>) -> Any?
    ): Any? {
        val commandStringWithoutCommandName = removeCommandNameFromCommandString(invokedCommandName, commandString)

        return try {
            val parameters = parseCommandParameters(command, commandStringWithoutCommandName, player)

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
        } catch (error: CommandParameterConverterParsingException) {
            CommandResult(
                error.message ?: "Unknown Parameter Conversion Exception Occurred",
                CommandResultStatus.Failed
            )
        } catch (error: CommandParameterValidationException) {
            CommandResult(
                error.message,
                CommandResultStatus.Failed
            )
        } catch (error: Exception) {
            // TODO: Add proper logging
            error.printStackTrace()
            CommandResult("Unknown Error Occurred", CommandResultStatus.Failed)
        }
    }

    private fun parseCommandParameters(
        command: CommandData,
        commandStringWithoutCommandName: String,
        player: Player?
    ): Map<KParameter, Any?> {
        val parameters: MutableMap<KParameter, Any?> =
            mutableMapOf(command.function.instanceParameter!! to command.handler)

        if (command.sides.contains(CommandSide.Client) && command.parametersType.isNotEmpty()) {
            parameters[command.parametersType[0].kParameter] = player
        }

        val parsedString = StringParser.parseToArray(commandStringWithoutCommandName)

        val isClientSupported = command.sides.contains(CommandSide.Client)

        val actualParametersSize =
            if (isClientSupported && command.parametersType.isNotEmpty())
                command.parametersType.size - 1
            else
                command.parametersType.size

        // TODO: Add Command Usage
        if (actualParametersSize < parsedString.size) {
            throw InvalidCommandParameterException("Too much parameters supplied. Usage: soon")
        }

        val errorMessages: MutableList<String> = mutableListOf()

        for (i in 0..<actualParametersSize) {
            val parameter = command.parametersType[i + if (isClientSupported) 1 else 0]

            if (i > parsedString.size - 1) {
                if (!parameter.isOptional)
                    errorMessages.add("Parameter ${parameter.name} is required and cannot be skipped")

                continue
            }

            val passedParameter = parsedString[i]

            if (passedParameter is SkipToken) {
                if (!parameter.isOptional)
                    errorMessages.add("Parameter ${parameter.name} is required and cannot be skipped")

                continue
            }

            try {
                if (passedParameter is StringToken) {
                    val output = parameterConverters[parameter.kClass]!!.parse(passedParameter.value)

                    parameter.validator.forEach {
                        val validator = parameterValidator[parameter.kClass]!![it.annotationClass]

                        @Suppress("UNCHECKED_CAST")
                        val isValid = (validator as CommandParameterValidator<Any>).invoke(it, output!!)

                        if (!isValid) {
                            val descriptionAnnotation =
                                it.annotationClass.findAnnotation<ParameterValidationDescription>()

                            val errorMessage = if (descriptionAnnotation != null)
                                parameterValidationDescriptionAnnotationToString(
                                    descriptionAnnotation,
                                    it,
                                    parameter.name
                                )
                            else
                                "Parameter validation for parameter ${parameter.name} failed."

                            errorMessages.add(errorMessage)
                        }
                    }

                    parameters[parameter.kParameter] = output!!
                }
            } catch (error: CommandParameterConverterParsingException) {
                errorMessages.add(error.toParametrizedString(parameter.name))
            }
        }

        if (errorMessages.isNotEmpty())
            throw CommandParameterValidationException(errorMessages.toTypedArray())

        return parameters.toMap()
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
                }
            else
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