package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class ByteParameter : CommandParameter<Byte> {
    override fun parse(input: String): Byte {
        try {
            return input.toByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into byte for parameter :parameterName:.")
        }
    }
}