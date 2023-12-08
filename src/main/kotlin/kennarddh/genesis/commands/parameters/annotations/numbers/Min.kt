package kennarddh.genesis.commands.parameters.annotations.numbers

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Min(val value: Long)
