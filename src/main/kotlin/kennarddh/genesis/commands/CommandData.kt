package kennarddh.genesis.commands

import kennarddh.genesis.handlers.Handler
import java.lang.reflect.Method
import kotlin.reflect.KClass

data class CommandData(
    val sides: Array<CommandSide>,
    val handler: Handler,
    val method: Method,
    val parameters: List<KClass<*>>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandData

        if (!sides.contentEquals(other.sides)) return false
        if (handler != other.handler) return false
        if (method != other.method) return false
        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sides.contentHashCode()
        result = 31 * result + handler.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}
