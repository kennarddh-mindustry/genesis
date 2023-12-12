package kennarddh.genesis.commands.parameters.converters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverterParsingException

class ByteParameterConverter : CommandParameterConverter<Byte> {
    override fun parse(input: String): Byte {
        try {
            return input.toByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into byte for parameter :parameterName:.")
        }
    }
}