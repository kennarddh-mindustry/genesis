package com.github.kennarddh.mindustry.genesis.core.commands

import arc.Events
import arc.struct.Seq
import arc.util.CommandHandler
import arc.util.Log
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.*
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.CommandValidation
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.CommandValidationDescription
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.commandValidationDescriptionAnnotationToString
import com.github.kennarddh.mindustry.genesis.core.commands.events.CommandsChanged
import com.github.kennarddh.mindustry.genesis.core.commands.exceptions.*
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.CommandParameterData
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.CommandParameterValidator
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.exceptions.CommandParameterValidationException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.exceptions.InvalidCommandParameterException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidation
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.parameterValidationDescriptionAnnotationToString
import com.github.kennarddh.mindustry.genesis.core.commands.result.CommandResult
import com.github.kennarddh.mindustry.genesis.core.commands.result.CommandResultStatus
import com.github.kennarddh.mindustry.genesis.core.commons.*
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.logging.Logger
import kotlinx.coroutines.*
import mindustry.Vars
import mindustry.gen.Player
import mindustry.server.ServerControl
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

class CommandRegistry {
    // TODO: This may not be reliable if any genesis controlled command is removed using Mindustry remove command instead of genesis one
    private val backingCommands: MutableList<CommandData> = mutableListOf()

    private val backingParameterTypes: MutableMap<KClass<*>, CommandParameter<*>> =
        mutableMapOf()
    private val parameterValidator: MutableMap<KClass<*>, MutableMap<KClass<*>, CommandParameterValidator<*>>> =
        mutableMapOf()
    private val commandValidator: MutableMap<KClass<*>, CommandValidator> = mutableMapOf()

    val commands
        get() = backingCommands.toList()

    val parameterTypes
        get() = backingParameterTypes.toMap()

    val clientHandler: CommandHandler
        get() = Vars.netServer.clientCommands

    val serverHandler: CommandHandler
        get() = ServerControl.instance.handler

    val clientCommands: Seq<CommandHandler.Command>
        get() = clientHandler.commandList

    val serverCommands: Seq<CommandHandler.Command>
        get() = serverHandler.commandList

    val genesisClientCommands
        get() = commands.filter { it.sides.contains(CommandSide.Client) }

    val genesisServerCommands
        get() = commands.filter { it.sides.contains(CommandSide.Server) }

    var clientPrefix: String
        get() = clientHandler.getPrefix()
        set(newPrefix) = clientHandler.setPrefix(newPrefix)

    var serverPrefix: String
        get() = serverHandler.getPrefix()
        set(newPrefix) = serverHandler.setPrefix(newPrefix)

    internal fun init() {
    }

    fun <T : Any, V : Any> registerParameterValidationAnnotation(
        annotation: KClass<T>,
        parametersType: List<KClass<out V>>,
        validator: CommandParameterValidator<V>
    ) {
        parametersType.forEach {
            if (!parameterValidator.contains(it)) parameterValidator[it] = mutableMapOf()

            parameterValidator[it]!![annotation] = validator
        }
    }

    fun <T : Any> registerCommandValidationAnnotation(
        annotation: KClass<T>,
        validator: CommandValidator
    ) {
        if (commandValidator.contains(annotation)) return

        commandValidator[annotation] = validator
    }

    fun <T : Any, V : Any> replaceParameterValidationAnnotationValidator(
        annotation: KClass<T>,
        parametersType: List<KClass<out V>>,
        validator: CommandParameterValidator<V>
    ) {
        parametersType.forEach {
            if (!parameterValidator.contains(it)) return@forEach
            if (!parameterValidator[it]!!.contains(annotation)) return@forEach

            parameterValidator[it]!![annotation] = validator
        }
    }

    fun <T : Any> replaceCommandValidationAnnotationValidator(
        annotation: KClass<T>,
        newValidator: CommandValidator
    ) {
        if (!commandValidator.contains(annotation)) return

        commandValidator[annotation] = newValidator
    }

    fun <T : Any> replaceParameterType(
        from: KClass<T>,
        parameterType: CommandParameter<T>
    ) {
        if (!backingParameterTypes.contains(from))
            throw NotFoundParameterTypeException("Parameter type for type ${from.qualifiedName} has not been registered")

        backingParameterTypes[from] = parameterType
    }

    fun <T : Any> registerParameterType(
        from: KClass<T>,
        parameterType: CommandParameter<T>
    ) {
        if (backingParameterTypes.contains(from))
            throw DuplicateParameterTypeException("Parameter type for type ${from.qualifiedName} has already been registered")

        backingParameterTypes[from] = parameterType
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

            val checkedNames: MutableList<String> = mutableListOf()

            for (name in names) {
                if (isServerSide && serverHandler.commandList.find { it.text == name } != null)
                    throw DuplicateCommandNameException("Command $name for method ${handler::class.qualifiedName}.${function.name} has already been registered for server")

                if (isClientSide && clientHandler.commandList.find { it.text == name } != null)
                    throw DuplicateCommandNameException("Command $name for method ${handler::class.qualifiedName}.${function.name} has already been registered for client")

                if (checkedNames.contains(name))
                    throw DuplicateCommandNameException("Method ${handler::class.qualifiedName}.${function.name} register $name command multiple times")

                checkedNames.add(name)
            }

            val functionParameters = function.parameters.drop(1)

            if (isClientSide && !isServerSide && functionParameters.isNotEmpty() && functionParameters[0].type == typeOf<Player?>())
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} support client only must accept non optional player as the first parameter")

            if (isClientSide && isServerSide && functionParameters.isNotEmpty() && (!functionParameters[0].isOptional || functionParameters[0].type != typeOf<Player?>()))
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} support client and server must accept optional player as the first parameter")

            val parameters: MutableList<CommandParameterData> = mutableListOf()

            val commandValidationAnnotations: List<Annotation> =
                function.annotations.filter { it.annotationClass.hasAnnotation<CommandValidation>() }

            commandValidationAnnotations.forEach {
                if (!commandValidator.containsKey(it.annotationClass))
                    throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} command with validator ${it.annotationClass} is not registered.")
            }

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
                    backingParameterTypes.filterKeys { (parameterTypeKClass as KClass<*>).isSubclassOf(it) }

                if (parameterTypeFilterResult.isEmpty())
                    throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${functionParameter.name} parameter with type $parameterTypeKClass is not registered.")

                val parameterValidationAnnotations: List<Annotation> =
                    functionParameter.annotations.filter { it.annotationClass.hasAnnotation<ParameterValidation>() }

                parameterValidationAnnotations.forEach {
                    if (parameterValidator[functionParameter.type.classifier]?.contains(it.annotationClass) != true)
                        throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${functionParameter.name} parameter with validator ${it.annotationClass} is not registered for ${parameterTypeKClass}.")
                }

                parameters.add(
                    CommandParameterData(
                        functionParameter,
                        parameterValidationAnnotations.toTypedArray(),
                    )
                )
            }

            val command = CommandData(
                this,
                names,
                description,
                brief,
                sides,
                handler,
                function,
                parameters.toTypedArray(),
                commandValidationAnnotations.toTypedArray()
            )

            backingCommands.add(command)

            names.forEach {
                val arcCommand = ArcCommand(this, it, description, brief, if (it == names[0]) null else names[0])

                if (isClientSide)
                    clientHandler.registerArcCommand(arcCommand)

                if (isServerSide)
                    serverHandler.registerArcCommand(arcCommand)
            }

            addedCommandCounter += 1
        }

        if (addedCommandCounter > 0)
            runOnMindustryThread {
                Events.fire(CommandsChanged())
            }
    }

    fun getCommandFromCommandName(commandName: String?): CommandData? {
        for (command in backingCommands) {
            if (command.names.contains(commandName)) return command
        }

        return null
    }

    /**
     * This method won't fail even if the command doesn't exist. It will just fail silently.
     */
    fun removeCommand(name: String, side: CommandSide) {
        val command = getCommandFromCommandName(name)

        if (side == CommandSide.Client) {
            clientHandler.removeCommand(name)

            if (command != null && command.sides.contains(CommandSide.Client))
                command.sides = command.sides.filter { it != CommandSide.Client }.toTypedArray()
        } else if (side == CommandSide.Server) {
            serverHandler.removeCommand(name)

            if (command != null && command.sides.contains(CommandSide.Server))
                command.sides = command.sides.filter { it != CommandSide.Server }.toTypedArray()
        }

        if (command != null && command.sides.isEmpty())
            backingCommands.remove(command)
    }

    /**
     * This method won't fail even if the command doesn't exist. It will just fail silently.
     */
    fun removeCommand(name: String) {
        removeCommand(name, CommandSide.Client)
        removeCommand(name, CommandSide.Server)
    }

    fun invokeServerCommand(command: String) {
        serverHandler.handleMessage(command)
    }

    fun invokeServerCommand(player: Player, command: String) {
        clientHandler.handleMessage(command, player)
    }

    fun invokeGenesisServerCommand(command: String) {
        if (command == "") throw IllegalArgumentException("Command cannot be empty")

        val result = command.split(' ', limit = 2)

        val name = result[0]
        val parametersString = if (result.size >= 2) result[1] else ""

        invokeCommand(name, parametersString, null)
    }

    fun invokeGenesisClientCommand(player: Player, command: String) {
        if (command == "") throw IllegalArgumentException("Command cannot be empty")

        val result = command.split(' ', limit = 2)

        val name = result[0]
        val parametersString = if (result.size >= 2) result[1] else ""

        invokeCommand(name, parametersString, player)
    }

    internal fun invokeCommand(name: String, parametersString: String, player: Player?) {
        val command = getCommandFromCommandName(name)

        if (
            command == null ||
            (player == null && !command.sides.contains(CommandSide.Server)) ||
            (player != null && !command.sides.contains(CommandSide.Client))
        ) {
            val result = CommandResult("Command $name not found.", CommandResultStatus.Failed)

            handleCommandHandlerResult(result, player)

            return
        }

        CoroutineScopes.Main.launch {
            val result = try {
                val deferredValidatorsResult = command.validator.map {
                    val validator = commandValidator[it.annotationClass]!!

                    async { validator.invoke(it, player) }
                }


                val validatorsResult = deferredValidatorsResult.awaitAll()

                command.validator.forEachIndexed { index, it ->
                    val isValid = validatorsResult[index]

                    if (!isValid) {
                        val descriptionAnnotation =
                            it.annotationClass.findAnnotation<CommandValidationDescription>()

                        val errorMessage =
                            if (descriptionAnnotation != null)
                                commandValidationDescriptionAnnotationToString(
                                    descriptionAnnotation,
                                    it,
                                    command.names[0]
                                )
                            else
                                "Command validation failed."

                        throw CommandValidationException(errorMessage)
                    }
                }

                val parameters = parseCommandParameters(command, parametersString, player)

                try {
                    command.function.callSuspendBy(parameters)
                } catch (error: Exception) {
                    Logger.error("Unknown Command Function Invoke Exception Occurred", error)

                    CommandResult("Unknown Error Occurred", CommandResultStatus.Failed)
                }
            } catch (error: InvalidCommandParameterException) {
                CommandResult(
                    error.message ?: "Unknown Invalid Command Parameter Exception Occurred",
                    CommandResultStatus.Failed
                )
            } catch (error: UnterminatedStringException) {
                CommandResult(
                    error.message ?: "Unknown Unterminated String Exception Occurred",
                    CommandResultStatus.Failed
                )
            } catch (error: InvalidEscapedCharacterException) {
                CommandResult(
                    error.message ?: "Unknown Escaped Character Exception Occurred",
                    CommandResultStatus.Failed
                )
            } catch (error: CommandParameterParsingException) {
                CommandResult(
                    error.message ?: "Unknown Parameter Conversion Exception Occurred",
                    CommandResultStatus.Failed
                )
            } catch (error: CommandValidationException) {
                CommandResult(
                    error.message ?: "Unknown Command Validation Exception Occurred",
                    CommandResultStatus.Failed
                )
            } catch (error: CommandParameterValidationException) {
                CommandResult(
                    error.message,
                    CommandResultStatus.Failed
                )
            } catch (error: Exception) {
                Logger.error("Unknown Invoke Command Exception Occurred", error)

                CommandResult("Unknown Error Occurred", CommandResultStatus.Failed)
            }

            handleCommandHandlerResult(result, player)
        }
    }

    private suspend fun parseCommandParameters(
        command: CommandData,
        commandStringWithoutCommandName: String,
        player: Player?
    ): Map<KParameter, Any?> = coroutineScope {
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

        if (actualParametersSize < parsedString.size) {
            throw InvalidCommandParameterException("Too much parameters supplied.")
        }

        val errorMessages = Collections.synchronizedList(mutableListOf<String>())

        val deferredCommandParameters: MutableList<Deferred<Unit>> = mutableListOf()

        for (i in 0..<actualParametersSize) {
            deferredCommandParameters.add(
                async {
                    val parameter = command.parametersType[i + if (isClientSupported) 1 else 0]

                    if (i > parsedString.size - 1) {
                        if (!parameter.isOptional)
                            errorMessages.add("Parameter ${parameter.name} is required and cannot be skipped.")

                        return@async
                    }

                    val passedParameter = parsedString[i]

                    if (passedParameter is SkipToken) {
                        if (!parameter.isOptional)
                            errorMessages.add("Parameter ${parameter.name} is required and cannot be skipped.")

                        return@async
                    }

                    try {
                        if (passedParameter is StringToken) {
                            val parameterTypeFilterResult =
                                backingParameterTypes.filterKeys { parameter.kClass.isSubclassOf(it) }

                            val parameterType = parameterTypeFilterResult.values.toTypedArray()[0]

                            @Suppress("UNCHECKED_CAST")
                            val output = (parameterType as CommandParameter<Any>).parse(
                                parameter.kClass as KClass<Any>,
                                passedParameter.value
                            )

                            parameters[parameter.kParameter] = output

                            val deferredValidatorsResult = parameter.validator.map {
                                val validator = parameterValidator[parameter.kClass]!![it.annotationClass]

                                async {
                                    @Suppress("UNCHECKED_CAST")
                                    (validator as CommandParameterValidator<Any>).invoke(it, output)
                                }
                            }

                            val validatorsResult = deferredValidatorsResult.awaitAll()

                            parameter.validator.forEachIndexed { index, it ->
                                val isValid = validatorsResult[index]

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
                        }
                    } catch (error: CommandParameterParsingException) {
                        errorMessages.add(error.toParametrizedString(parameter.name))
                    }
                }
            )
        }

        deferredCommandParameters.awaitAll()

        if (errorMessages.isNotEmpty()) {
            val prefix = if (player == null) serverPrefix else clientPrefix

            val fullUsage = "${prefix}${command.names[0]} ${command.toUsage()}"

            errorMessages.add("Usage: \"$fullUsage\"")

            throw CommandParameterValidationException(errorMessages.toTypedArray())
        }

        return@coroutineScope parameters.toMap()
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

            runOnMindustryThread {
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