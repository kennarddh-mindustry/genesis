package kennarddh.genesis.commands.parameters.annotations.numbers

fun <T : Number> validateMin(annotation: Annotation, value: T): Boolean {
    val min = (annotation as Min).value

    return value.toLong() > min
}


@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Min(val value: Long)
