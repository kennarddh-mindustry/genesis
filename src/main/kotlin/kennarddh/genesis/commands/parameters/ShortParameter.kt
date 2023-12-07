package kennarddh.genesis.commands.parameters

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class ShortParameter : CommandParameter<Short> {
    override fun parse(input: String): Short {
        try {
            return input.toShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into short for parameter :parameterName:.")
        }
    }
}