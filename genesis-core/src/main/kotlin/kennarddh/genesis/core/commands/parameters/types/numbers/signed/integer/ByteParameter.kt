package kennarddh.genesis.core.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class ByteParameter : CommandParameter<Byte> {
    override fun parse(input: String): Byte {
        try {
            return input.toByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into byte for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: Byte): String = "byte"
}