package kennarddh.genesis.commands.parameters.validations.numbers

import kennarddh.genesis.commands.parameters.validations.ParameterValidation

fun <T : Number> validateMax(annotation: Annotation, value: T): Boolean {
    val max = (annotation as Max).value

    return value.toLong() < max
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
annotation class Max(val value: Long)
