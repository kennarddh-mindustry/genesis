package kennarddh.genesis.commands.parameters

import kotlin.reflect.KClass

data class CommandParameter(
    val type: KClass<*>,
    val name: String,
    val validator: Array<Annotation>,
    val isOptional: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandParameter

        if (type != other.type) return false
        if (name != other.name) return false
        if (!validator.contentEquals(other.validator)) return false
        if (isOptional != other.isOptional) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + validator.contentHashCode()
        result = 31 * result + isOptional.hashCode()
        return result
    }
}