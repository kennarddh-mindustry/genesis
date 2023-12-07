package kennarddh.genesis.commands.parameters.base

interface CommandParameter<T> {
    fun parse(input: String): T
}