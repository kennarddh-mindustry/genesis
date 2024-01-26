package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.validations.numbers

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidation
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations.ParameterValidationDescription

fun <T : Number> validateGTE(annotation: Annotation, value: T): Boolean {
    val max = (annotation as GTE).value

    return value.toLong() >= max
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
@ParameterValidationDescription("Parameter :parameterName: must be greater than or equal to :value:")
annotation class GTE(val value: Long)
