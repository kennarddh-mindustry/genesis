package kennarddh.genesis.core.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class LongParameter : CommandParameter<Long> {
    override fun parse(input: String): Long {
        try {
            return input.toLong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into long for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: Long): String = "long"
}