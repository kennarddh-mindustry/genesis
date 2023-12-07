package kennarddh.genesis.commands.parameters.numbers.unsigned.integer

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException

class UShortParameterConverter : CommandParameterConverter<UShort> {
    override fun parse(input: String): UShort {
        try {
            return input.toUShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into unsigned short for parameter :parameterName:.")
        }
    }
}