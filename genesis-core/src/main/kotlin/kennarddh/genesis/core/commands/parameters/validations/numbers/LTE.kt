package kennarddh.genesis.core.commands.parameters.validations.numbers

import kennarddh.genesis.core.commands.parameters.validations.ParameterValidation
import kennarddh.genesis.core.commands.parameters.validations.ParameterValidationDescription

fun <T : Number> validateLTE(annotation: Annotation, value: T): Boolean {
    val annotationValue = (annotation as LTE).value

    return value.toLong() <= annotationValue
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
@ParameterValidationDescription("Parameter :parameterName: must be less than or equal to :value:")
annotation class LTE(val value: Long)
