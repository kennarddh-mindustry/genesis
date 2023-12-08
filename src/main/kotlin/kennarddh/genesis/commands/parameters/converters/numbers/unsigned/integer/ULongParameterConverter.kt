package kennarddh.genesis.commands.parameters.converters.numbers.unsigned.integer

import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverterParsingException

class ULongParameterConverter : CommandParameterConverter<ULong> {
    override fun parse(input: String): ULong {
        try {
            return input.toULong()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into unsigned long for parameter :parameterName:.")
        }
    }
}