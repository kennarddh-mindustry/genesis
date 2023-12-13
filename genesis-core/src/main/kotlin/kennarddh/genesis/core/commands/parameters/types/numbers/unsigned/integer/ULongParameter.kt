package kennarddh.genesis.core.commands.parameters.types.numbers.unsigned.integer

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class ULongParameter : CommandParameter<ULong> {
    override fun parse(input: String): ULong {
        try {
            return input.toULong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned long for parameter :parameterName:.")
        }
    }
}