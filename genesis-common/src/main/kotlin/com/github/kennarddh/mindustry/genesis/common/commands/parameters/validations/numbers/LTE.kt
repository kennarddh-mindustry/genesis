package com.github.kennarddh.mindustry.genesis.common.commands.parameters.validations.numbers

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidation
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription

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
