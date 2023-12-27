package kennarddh.genesis.core.commands

import arc.Core
import arc.Events
import arc.util.Log
import kennarddh.genesis.core.commands.annotations.*
import kennarddh.genesis.core.commands.events.CommandsChanged
import kennarddh.genesis.core.commands.exceptions.DuplicateCommandNameException
import kennarddh.genesis.core.commands.exceptions.InvalidCommandMethodException
import kennarddh.genesis.core.commands.parameters.CommandParameterData
import kennarddh.genesis.core.commands.parameters.CommandParameterValidator
import kennarddh.genesis.core.commands.parameters.exceptions.CommandParameterValidationException
import kennarddh.genesis.core.commands.parameters.exceptions.InvalidCommandParameterException
import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidation
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidationDescription
import kennarddh.genesis.core.commands.parameters.validations.parameterValidationDescriptionAnnotationToString
import kennarddh.genesis.core.commands.result.CommandResult
import kennarddh.genesis.core.commands.result.CommandResultStatus
import kennarddh.genesis.core.commons.*
import kennarddh.genesis.core.handlers.Handler
import mindustry.Vars
import mindustry.gen.Player
import mindustry.server.ServerControl
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

class CommandRegistry {
    private val commands: MutableList<CommandData> = mutableListOf()

    private val _parameterTypes: MutableMap<KClass<*>, CommandParameter<*>> =
        mutableMapOf()
    private val parameterValidator: MutableMap<KClass<*>, MutableMap<KClass<*>, CommandParameterValidator<*>>> =
        mutableMapOf()

    val parameterTypes
        get() = _parameterTypes.toMap()

    val clientCommands
        get() = commands.filter { it.sides.contains(CommandSide.Client) }

    val serverCommands
        get() = commands.filter { it.sides.contains(CommandSide.Server) }

    val clientHandler
        get() = Vars.netServer.clientCommands

    val serverHandler
        get() = ServerControl.instance.handler

    @Suppress("UNUSED")
    var clientPrefix: String
        get() = clientHandler.getPrefix()
        set(newPrefix) = clientHandler.setPrefix(newPrefix)

    @Suppress("UNUSED")
    var serverPrefix: String
        get() = serverHandler.getPrefix()
        set(newPrefix) = serverHandler.setPrefix(newPrefix)

    internal fun init() {
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

    fun registerParameterType(
        from: KClass<*>,
        parameterType: CommandParameter<*>
    ) {
        // TODO: Check if parameter type already registered

        _parameterTypes[from] = parameterType
    }

    fun registerHandler(handler: Handler) {
        var addedCommandCounter = 0

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

            val parameters: MutableList<CommandParameterData> = mutableListOf()

            functionParameters.forEachIndexed { index, functionParameter ->
                if (isClientSide && index == 0) {
                    parameters.add(
                        CommandParameterData(
                            functionParameter,
                            arrayOf(),
                        )
                    )

                    return@forEachIndexed
                }

                val parameterTypeKClass = functionParameter.type.classifier

                val parameterTypeFilterResult =
                    _parameterTypes.filterKeys { (parameterTypeKClass as KClass<*>).isSubclassOf(it) }

                if (parameterTypeFilterResult.isEmpty())
                    throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${functionParameter.name} parameter with type $parameterTypeKClass is not registered.")

                val validationsAnnotation: List<Annotation> =
                    functionParameter.annotations.filter { it.annotationClass.hasAnnotation<ParameterValidation>() }

                validationsAnnotation.forEach {
                    if (parameterValidator[functionParameter.type.classifier]?.contains(it.annotationClass) != true)
                        throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${functionParameter.name} parameter with validator ${it.annotationClass} is not registered for ${parameterTypeKClass}.")
                }

                parameters.add(
                    CommandParameterData(
                        functionParameter,
                        validationsAnnotation.toTypedArray(),
                    )
                )
            }

            commands.add(
                CommandData(
                    this,
                    names,
                    description,
                    brief,
                    sides,
                    handler,
                    function,
                    parameters.toTypedArray()
                )
            )

            names.forEach {
                if (isClientSide)
                    clientHandler.register(it, "[params...]", "", ArcCommandRunner(this, it))

                if (isServerSide)
                    serverHandler.register(it, "[params...]", "", ArcCommandRunner(this, it))
            }

            addedCommandCounter += 1
        }

        if (addedCommandCounter > 0)
            Events.fire(CommandsChanged())
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

            try {
                invoke(parameters)
            } catch (e: Exception) {
                //TODO: Proper command stack trace handling
                Log.err("Command exception occurred")
                e.printStackTrace()
            }
        } catch (error: InvalidCommandParameterException) {
            CommandResult(
                error.message ?: "Unknown Invalid Command Parameter Exception Occurred",
                CommandResultStatus.Failed
            )
        } catch (error: UnterminatedStringException) {
            CommandResult(error.message ?: "Unknown Unterminated String Exception Occurred", CommandResultStatus.Failed)
        } catch (error: InvalidEscapedCharacterException) {
            CommandResult(error.message ?: "Unknown Escaped Character Exception Occurred", CommandResultStatus.Failed)
        } catch (error: CommandParameterParsingException) {
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
                    val parameterTypeFilterResult =
                        _parameterTypes.filterKeys { parameter.kClass.isSubclassOf(it) }

                    val parameterType = parameterTypeFilterResult.values.toTypedArray()[0]

                    @Suppress("UNCHECKED_CAST")
                    val output = (parameterType as CommandParameter<Any>).parse(
                        parameter.kClass as KClass<Any>,
                        passedParameter.value
                    )

                    parameter.validator.forEach {
                        val validator = parameterValidator[parameter.kClass]!![it.annotationClass]

                        @Suppress("UNCHECKED_CAST")
                        val isValid = (validator as CommandParameterValidator<Any>).invoke(it, output)

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

                    parameters[parameter.kParameter] = output
                }
            } catch (error: CommandParameterParsingException) {
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