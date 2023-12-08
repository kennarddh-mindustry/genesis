package kennarddh.genesis.commands

import kotlin.reflect.KClass

typealias CommandParameterValidator<T> = (annotation: Annotation, value: T) -> Boolean

data class CommandParameter(
    val type: KClass<*>,
    val name: String,
    val validator: Array<Annotation>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandParameter

        if (type != other.type) return false
        if (!validator.contentEquals(other.validator)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + validator.contentHashCode()
        return result
    }
}
