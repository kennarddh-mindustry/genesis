package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class UByteParameter : CommandParameter<UByte> {
    override fun parse(input: String): UByte {
        try {
            return input.toUByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned byte for parameter :parameterName:.")
        }
    }
}