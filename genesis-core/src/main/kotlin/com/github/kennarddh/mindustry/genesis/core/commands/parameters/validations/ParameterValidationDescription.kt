package com.github.kennarddh.mindustry.genesis.core.commands.parameters.validations

import kotlin.reflect.full.memberProperties

fun parameterValidationDescriptionAnnotationToString(
    descriptionAnnotation: ParameterValidationDescription,
    validationAnnotation: Annotation,
    parameterName: String
): String {
    var output = descriptionAnnotation.description.replace(":parameterName:", parameterName)

    val properties = validationAnnotation.annotationClass.memberProperties

    properties.forEach {
        output = output.replace(":${it.name}:", it.getter.call(validationAnnotation).toString())
    }

    return output
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ParameterValidationDescription(val description: String)
