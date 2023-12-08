package kennarddh.genesis.commands.parameters.annotations.numbers

fun <T : Number> validateMax(annotation: Annotation, value: T): Boolean {
    val max = (annotation as Max).value

    return value.toLong() < max
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ParameterValidation
annotation class Max(val value: Long)
