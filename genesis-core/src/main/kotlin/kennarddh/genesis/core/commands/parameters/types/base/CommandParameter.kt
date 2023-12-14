package kennarddh.genesis.core.commands.parameters.types.base

interface CommandParameter<T> {
    fun parse(input: String): T

    fun toUsageType(input: T): String
}