package kennarddh.genesis.commands.parameters.validations.numbers

import kennarddh.genesis.commands.parameters.validations.ParameterValidation

fun <T : Number> validateMin(annotation: Annotation, value: T): Boolean {
    val min = (annotation as Min).value

    return value.toLong() > min
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
annotation class Min(val value: Long)
