package kennarddh.genesis.core.commands.parameters.converters.numbers.unsigned.integer

import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverterParsingException

class UByteParameterConverter : CommandParameterConverter<UByte> {
    override fun parse(input: String): UByte {
        try {
            return input.toUByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into unsigned byte for parameter :parameterName:.")
        }
    }
}