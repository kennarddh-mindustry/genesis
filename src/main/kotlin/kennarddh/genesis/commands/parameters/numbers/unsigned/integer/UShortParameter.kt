package kennarddh.genesis.commands.parameters.numbers.unsigned.integer

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class UShortParameter : CommandParameter<UShort> {
    override fun parse(input: String): UShort {
        try {
            return input.toUShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned short for parameter :parameterName:.")
        }
    }
}