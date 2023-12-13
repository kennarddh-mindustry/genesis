package kennarddh.genesis.core.commands.parameters.validations.numbers

import kennarddh.genesis.core.commands.parameters.validations.ParameterValidation
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidationDescription

fun <T : Number> validateGT(annotation: Annotation, value: T): Boolean {
    val annotationValue = (annotation as GT).value

    return value.toLong() > annotationValue
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
@ParameterValidationDescription("Parameter :parameterName: must be greater than :value:")
annotation class GT(val value: Long)
