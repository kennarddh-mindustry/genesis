package kennarddh.genesis.core.commands

import kennarddh.genesis.core.commands.parameters.CommandParameterData
import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.handlers.Handler
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSubclassOf

data class CommandData(
    val commandRegistry: CommandRegistry,
    val names: Array<String>,
    val description: String,
    val brief: String,
    val sides: Array<CommandSide>,
    val handler: Handler,
    val function: KFunction<*>,
    val parametersType: Array<CommandParameterData>
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

    fun toUsage(): String {
        val output = StringBuilder()

        val functionParameters =
            if (sides.contains(CommandSide.Client)) parametersType.drop(1).toTypedArray() else parametersType

        functionParameters.forEach {
            output.append(if (it.isOptional) "[" else "<")
            output.append(it.name)
            output.append(":")

            val parameterTypeFilterResult =
                commandRegistry.parameterTypes.filterKeys { type -> it.kClass.isSubclassOf(type) }

            val parameterType = parameterTypeFilterResult.values.toTypedArray()[0]

            output.append(
                @Suppress("UNCHECKED_CAST")
                (parameterType as CommandParameter<Any>).toUsageType(it.kClass as KClass<Any>)
            )

            output.append(if (it.isOptional) "]" else ">")

            output.append(' ')
        }

        return output.trimEnd(' ').toString()
    }
}
