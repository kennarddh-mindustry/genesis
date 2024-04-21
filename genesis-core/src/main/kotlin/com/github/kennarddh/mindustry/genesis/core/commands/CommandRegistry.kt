package com.github.kennarddh.mindustry.genesis.core.commands

import arc.Events
import arc.struct.Seq
import arc.util.CommandHandler
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Brief
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Command
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.Description
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.CommandValidation
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.CommandValidationDescription
import com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations.commandValidationDescriptionAnnotationToString
import com.github.kennarddh.mindustry.genesis.core.commands.events.CommandsChanged
import com.github.kennarddh.mindustry.genesis.core.commands.exceptions.DuplicateCommandNameException
import com.github.kennarddh.mindustry.genesis.core.commands.exceptions.DuplicateParameterTypeException
import com.github.kennarddh.mindustry.genesis.core.commands.exceptions.InvalidCommandMethodException
import com.github.kennarddh.mindustry.genesis.core.commands.exceptions.NotFoundParameterTypeException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.CommandParameterData
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.CommandParameterValidator
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.exceptions.CommandParameterValidationException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.exceptions.InvalidCommandParameterException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidation
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.parameterValidationDescriptionAnnotationToString
import com.github.kennarddh.mindustry.genesis.core.commands.senders.CommandSender
import com.github.kennarddh.mindustry.genesis.core.commands.senders.PlayerCommandSender
import com.github.kennarddh.mindustry.genesis.core.commands.senders.ServerCommandSender
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

            val descriptionAnnotation = function.findAnnotation<Description>()
            val briefAnnotation = function.findAnnotation<Brief>()

            val names = commandAnnotation.names

            val description = descriptionAnnotation?.description ?: ""
            val brief = briefAnnotation?.brief ?: description

            // The first parameter is dropped because it's the instance
            val functionParameters = function.parameters.drop(1)

            // TODO: Cache createType() result
            if (!functionParameters[0].type.isSubtypeOf(CommandSender::class.createType())) {
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} first parameter must be CommandSender or it's subclass.")
            }

            if (functionParameters[0].isVararg) {
                throw InvalidCommandMethodException("Method ${handler::class.qualifiedName}.${function.name} sender parameter cannot be vararg.")
            }

            val sides: Set<CommandSide> =
                if (functionParameters[0].type == ServerCommandSender::class.createType()) {
                    setOf(CommandSide.Server)
                } else if (functionParameters[0].type.isSubtypeOf(PlayerCommandSender::class.createType())) {
                    setOf(CommandSide.Client)
                } else {
                    setOf(CommandSide.Server, CommandSide.Client)
                }

            val checkedNames: MutableList<String> = mutableListOf()

            for (name in names) {
                if (sides.contains(CommandSide.Server) && serverHandler.commandList.find { it.text == name } != null)
                    throw DuplicateCommandNameException("Command $name for method ${handler::class.qualifiedName}.${function.name} has already been registered for server side.")

                if (sides.contains(CommandSide.Client) && clientHandler.commandList.find { it.text == name } != null)
                    throw DuplicateCommandNameException("Command $name for method ${handler::class.qualifiedName}.${function.name} has already been registered for client side.")

                if (checkedNames.contains(name))
                    throw DuplicateCommandNameException("Method ${handler::class.qualifiedName}.${function.name} register $name command multiple times")

                checkedNames.add(name)
            }

            val commandParameters = functionParameters.drop(1)

            val parameters: MutableList<CommandParameterData> = mutableListOf()

            val commandValidationAnnotations: List<Annotation> =
                function.annotations.filter { it.annotationClass.hasAnnotation<CommandValidation>() }

            commandValidationAnnotations.forEach {
                if (!commandValidator.containsKey(it.annotationClass))
                    throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} command with validator ${it.annotationClass} is not registered.")
            }

            commandParameters.forEachIndexed { index, commandParameter ->
                val parameterTypeKClass = commandParameter.type.classifier

                val parameterTypeFilterResult =
                    backingParameterTypes.filterKeys { (parameterTypeKClass as KClass<*>).isSubclassOf(it) }

                if (parameterTypeFilterResult.isEmpty())
                    throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${commandParameter.name} parameter with type $parameterTypeKClass is not registered.")

                val parameterValidationAnnotations: List<Annotation> =
                    commandParameter.annotations.filter { it.annotationClass.hasAnnotation<ParameterValidation>() }

                parameterValidationAnnotations.forEach {
                    if (parameterValidator[commandParameter.type.classifier]?.contains(it.annotationClass) != true)
                        throw InvalidCommandParameterException("Method ${handler::class.qualifiedName}.${function.name} ${commandParameter.name} parameter with validator ${it.annotationClass} is not registered for ${parameterTypeKClass}.")
                }

                parameters.add(
                    CommandParameterData(
                        commandParameter,
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

                if (sides.contains((CommandSide.Server)))
                    clientHandler.registerArcCommand(arcCommand)

                if (sides.contains((CommandSide.Client)))
                    serverHandler.registerArcCommand(arcCommand)
            }

            addedCommandCounter += 1
        }

        if (addedCommandCounter > 0)
            runOnMindustryThread {
                Events.fire(CommandsChanged())
            }
    }

    fun getCommandFromCommandName(commandName: String): CommandData? {
        for (command in backingCommands) {
            if (command.names.contains(commandName)) return command
        }

        return null
    }

    /**
     * This method won't fail even if the command doesn't exist. It will just fail silently.
     */
    fun removeCommand(name: String, vararg sidesToBeRemoved: CommandSide) {
        val command = getCommandFromCommandName(name)

        var removedCounter = 0

        if (sidesToBeRemoved.contains(CommandSide.Client)) {
            val exist = clientHandler.commandList.contains { it.text == name }

            if (exist) {
                removedCounter += 1

                clientHandler.removeCommand(name)

                if (command != null && command.sides.contains(CommandSide.Client)) {
                    val mutableCommandSides = command.sides.toMutableSet()

                    mutableCommandSides.remove(CommandSide.Client)

                    command.sides = mutableCommandSides.toSet()
                }
            }
        } else if (sidesToBeRemoved.contains(CommandSide.Server)) {
            val exist = serverHandler.commandList.contains { it.text == name }

            if (exist) {
                removedCounter += 1

                serverHandler.removeCommand(name)

                if (command != null && command.sides.contains(CommandSide.Server)) {
                    val mutableCommandSides = command.sides.toMutableSet()

                    mutableCommandSides.remove(CommandSide.Server)

                    command.sides = mutableCommandSides.toSet()
                }
            }
        }

        if (command != null && command.sides.isEmpty())
            backingCommands.remove(command)

        if (removedCounter > 0)
            runOnMindustryThread {
                Events.fire(CommandsChanged())
            }
    }

    /**
     * This method won't fail even if the command doesn't exist. It will just fail silently.
     *
     * Remove command from all sides.
     */
    fun removeCommand(name: String) {
        removeCommand(name, CommandSide.Server, CommandSide.Client)
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

        val sender = if (player != null) {
            PlayerCommandSender(player)
        } else {
            ServerCommandSender()
        }

        if (command == null ||
            (player == null && !command.sides.contains(CommandSide.Server)) ||
            (player != null && !command.sides.contains(CommandSide.Client))
        )
            return sender.sendError("Command $name not found.")

        CoroutineScopes.Main.launch {
            val deferredValidatorsResult = command.validator.map {
                val validator = commandValidator[it.annotationClass]!!

                async {
                    try {
                        CommandParameterValidationResult(validator, validator.invoke(it, player), null)
                    } catch (exception: Exception) {
                        CommandParameterValidationResult(validator, false, exception)
                    }
                }
            }

            val validatorsResult = deferredValidatorsResult.awaitAll()

            command.validator.forEachIndexed { index, it ->
                val commandParameterValidationResult = validatorsResult[index]

                if (!commandParameterValidationResult.isValid) {
                    val descriptionAnnotation =
                        it.annotationClass.findAnnotation<CommandValidationDescription>()

                    val errorMessage =
                        if (commandParameterValidationResult.exception != null) {
                            Logger.error(
                                "Command parameter validation \"${commandParameterValidationResult.validator::class.qualifiedName} \" should not throw exception.",
                                commandParameterValidationResult.exception
                            )

                            "Unknown error occurred while validating command's parameter"
                        } else if (descriptionAnnotation != null)
                            commandValidationDescriptionAnnotationToString(
                                descriptionAnnotation,
                                it,
                                command.names[0]
                            )
                        else
                            "Command validation failed."


                    return@launch sender.sendError(errorMessage)
                }
            }

            val parameters = try {
                parseCommandParameters(command, parametersString, player)
            } catch (exception: CommandParameterValidationException) {
                return@launch sender.sendError(exception.message)
            } catch (error: UnterminatedStringException) {
                return@launch sender.sendError(
                    error.message ?: "Unknown Unterminated String Exception Occurred",
                )
            } catch (error: InvalidEscapedCharacterException) {
                return@launch sender.sendError(
                    error.message ?: "Unknown Escaped Character Exception Occurred",
                )
            } catch (error: InvalidCommandParameterException) {
                return@launch sender.sendError(
                    error.message ?: "Unknown Invalid Command Parameter Exception Occurred",
                )
            }

            try {
                command.function.callSuspendBy(
                    mapOf(command.function.instanceParameter!! to command.handler) + parameters
                )
            } catch (error: Exception) {
                Logger.error("Unknown Command Function Invoke Exception Occurred", error)

                return@launch sender.sendError("Unknown Error Occurred")
            }
        }
    }

    private suspend fun parseCommandParameters(
        command: CommandData,
        commandStringWithoutCommandName: String,
        player: Player?
    ): Map<KParameter, Any?> = coroutineScope {
        val parameters: MutableMap<KParameter, Any?> = mutableMapOf()

        val parsedString = StringParser.parseToArray(commandStringWithoutCommandName)

        if (command.parametersType.size < parsedString.size) {
            throw InvalidCommandParameterException("Too much parameters supplied.")
        }

        val errorMessages = Collections.synchronizedList(mutableListOf<String>())

        val deferredCommandParameters: MutableList<Deferred<Unit>> = mutableListOf()

        for (i in 0..<command.parametersType.size) {
            deferredCommandParameters.add(
                async {
                    val parameter = command.parametersType[i]

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
                    } catch (error: Exception) {
                        Logger.error("Unknown Parameter Command Exception Occurred")

                        errorMessages.add("Unknown Error Occurred.")
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
}