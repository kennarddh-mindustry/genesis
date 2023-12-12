package kennarddh.genesis.core.commands.parameters.validations.numbers

import kennarddh.genesis.core.commands.parameters.validations.ParameterValidation
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidationDescription

fun <T : Number> validateMax(annotation: Annotation, value: T): Boolean {
    val max = (annotation as Max).value

    return value.toLong() < max
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
@ParameterValidationDescription("Parameter :parameterName: must be less than :value:")
annotation class Max(val value: Long)
