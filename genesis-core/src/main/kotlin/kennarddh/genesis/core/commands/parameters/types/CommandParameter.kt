package kennarddh.genesis.core.commands.parameters.types

import kotlin.reflect.KClass

interface CommandParameter<T : Any> {
    fun parse(input: String): T

    fun toUsageType(input: KClass<T>): String
}