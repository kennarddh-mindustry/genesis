package kennarddh.genesis.common.commands.parameters.types

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kotlin.reflect.KClass

class StringParameter : CommandParameter<String> {
    override fun parse(input: String): String {
        return input
    }

    override fun toUsageType(input: KClass<String>): String = "string"
}