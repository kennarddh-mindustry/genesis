package kennarddh.genesis.commands

typealias CommandParameterValidator<T> = (annotation: Annotation, value: T) -> Boolean

