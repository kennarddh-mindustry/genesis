package com.github.kennarddh.mindustry.genesis.core.commands

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.CommandParameterData
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import mindustry.gen.Player
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf

typealias CommandValidator = suspend (annotation: Annotation, player: Player?) -> Boolean

data class CommandData(
    val commandRegistry: CommandRegistry,
    val names: Array<String>,
    val description: String,
    val brief: String,
    var sides: Array<CommandSide>,
    val handler: Handler,
    val function: KFunction<*>,
    val parametersType: Array<CommandParameterData>,
    val validator: Array<Annotation>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandData

        if (!names.contentEquals(other.names)) return false
        if (description != other.description) return false
        if (brief != other.brief) return false
        if (!sides.contentEquals(other.sides)) return false
        if (handler != other.handler) return false
        if (function != other.function) return false
        if (!parametersType.contentEquals(other.parametersType)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = names.contentHashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + brief.hashCode()
        result = 31 * result + sides.contentHashCode()
        result = 31 * result + handler.hashCode()
        result = 31 * result + function.hashCode()
        result = 31 * result + parametersType.contentHashCode()
        return result
    }

    fun toUsage(): String = buildString {
        val functionParameters =
            if (sides.contains(CommandSide.Client)) parametersType.drop(1).toTypedArray() else parametersType

        functionParameters.forEach {
            append(if (it.isOptional) "[" else "<")
            append(it.name)
            append(":")

            val parameterTypeFilterResult =
                commandRegistry.parameterTypes.filterKeys { type -> it.kClass.isSubclassOf(type) }

            val parameterType = parameterTypeFilterResult.values.toTypedArray()[0]

            append(
                @Suppress("UNCHECKED_CAST")
                (parameterType as CommandParameter<Any>).toUsageType(it.kClass as KClass<Any>)
            )

            append(if (it.isOptional) "]" else ">")

            append(' ')
        }

        trimEnd(' ')
    }
}
