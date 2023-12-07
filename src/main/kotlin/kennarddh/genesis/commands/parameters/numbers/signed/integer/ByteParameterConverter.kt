package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException

class ByteParameterConverter : CommandParameterConverter<Byte> {
    override fun parse(input: String): Byte {
        try {
            return input.toByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into byte for parameter :parameterName:.")
        }
    }
}