package kennarddh.genesis.core.commands.parameters

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

typealias CommandParameterValidator<T> = (annotation: Annotation, value: T) -> Boolean

data class CommandParameterData(
    val kParameter: KParameter,
    val validator: Array<Annotation>,
) {
    val name: String
        get() = kParameter.name ?: "Unknown Parameter"

    val isOptional: Boolean
        get() = kParameter.isOptional

    val kClass: KClass<*>
        get() = kParameter.type.classifier as KClass<*>

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommandParameterData

        if (kParameter != other.kParameter) return false
        if (!validator.contentEquals(other.validator)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = kParameter.hashCode()
        result = 31 * result + validator.contentHashCode()
        return result
    }
}