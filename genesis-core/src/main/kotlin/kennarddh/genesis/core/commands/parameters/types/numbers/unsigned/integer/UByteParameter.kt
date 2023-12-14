package kennarddh.genesis.core.commands.parameters.types.numbers.unsigned.integer

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class UByteParameter : CommandParameter<UByte> {
    override fun parse(input: String): UByte {
        try {
            return input.toUByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned byte for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: UByte): String = "uByte"
}