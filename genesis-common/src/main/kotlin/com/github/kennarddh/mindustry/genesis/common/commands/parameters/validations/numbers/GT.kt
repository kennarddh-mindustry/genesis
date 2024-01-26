package com.github.kennarddh.mindustry.genesis.common.commands.parameters.validations.numbers

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidation
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription

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
