package kennarddh.genesis.commands.parameters.base

interface CommandParameterConverter<T> {
    fun parse(input: String): T
}