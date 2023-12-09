package kennarddh.genesis.commands.parameters.validations.numbers

import kennarddh.genesis.commands.parameters.validations.ParameterValidation
import kennarddh.genesis.commands.parameters.validations.ParameterValidationDescription

fun <T : Number> validateMin(annotation: Annotation, value: T): Boolean {
    val min = (annotation as Min).value

    return value.toLong() > min
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
@ParameterValidationDescription("Parameter :parameterName: must be greater than :value:")
annotation class Min(val value: Long)
