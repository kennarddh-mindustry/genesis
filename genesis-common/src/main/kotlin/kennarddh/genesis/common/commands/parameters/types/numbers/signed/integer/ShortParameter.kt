package kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class ShortParameter : CommandParameter<Short> {
    override fun parse(instance: KClass<Short>, input: String): Short {
        try {
            return input.toShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into short for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<Short>): String = "short"
}