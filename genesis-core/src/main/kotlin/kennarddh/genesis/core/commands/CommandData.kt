package kennarddh.genesis.core.commands

import kennarddh.genesis.core.commands.parameters.CommandParameterData
import kennarddh.genesis.core.handlers.Handler
import kotlin.reflect.KFunction

data class CommandData(
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
}
