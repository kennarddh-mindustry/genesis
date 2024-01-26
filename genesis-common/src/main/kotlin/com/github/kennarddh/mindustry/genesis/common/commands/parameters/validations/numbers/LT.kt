package com.github.kennarddh.mindustry.genesis.common.commands.parameters.validations.numbers

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidation
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription

fun <T : Number> validateLT(annotation: Annotation, value: T): Boolean {
    val annotationValue = (annotation as LT).value

    return value.toLong() < annotationValue
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
@ParameterValidationDescription("Parameter :parameterName: must be less than :value:")
annotation class LT(val value: Long)
