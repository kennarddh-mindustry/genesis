package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException

class ShortParameterConverter : CommandParameterConverter<Short> {
    override fun parse(input: String): Short {
        try {
            return input.toShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into short for parameter :parameterName:.")
        }
    }
}