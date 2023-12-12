package kennarddh.genesis.commands.parameters.converters.base

interface CommandParameterConverter<T> {
    fun parse(input: String): T
}