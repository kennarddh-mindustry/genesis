package com.github.kennarddh.mindustry.genesis.core.commands.annotations.validations

import kotlin.reflect.full.memberProperties

fun commandValidationDescriptionAnnotationToString(
    descriptionAnnotation: CommandValidationDescription,
    validationAnnotation: Annotation,
    commandName: String
): String {
    var output = descriptionAnnotation.description.replace(":commandName:", commandName)

    val properties = validationAnnotation.annotationClass.memberProperties

    properties.forEach {
        output = output.replace(":${it.name}:", it.getter.call(validationAnnotation).toString())
    }

    return output
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CommandValidationDescription(val description: String)
