package kennarddh.genesis.commands

import kennarddh.genesis.handlers.Handler
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class CommandData(
    val sides: Array<CommandSide>,
    val handler: Handler,
    val function: KFunction<*>,
    val parameters: List<KClass<*>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandData

        if (!sides.contentEquals(other.sides)) return false
        if (handler != other.handler) return false
        if (function != other.function) return false
        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sides.contentHashCode()
        result = 31 * result + handler.hashCode()
        result = 31 * result + function.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}
