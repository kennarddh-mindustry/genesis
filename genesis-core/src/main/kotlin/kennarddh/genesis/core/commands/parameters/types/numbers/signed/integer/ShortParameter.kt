package kennarddh.genesis.core.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class ShortParameter : CommandParameter<Short> {
    override fun parse(input: String): Short {
        try {
            return input.toShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into short for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: Short): String = "short"
}