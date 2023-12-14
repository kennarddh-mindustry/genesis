package kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class ByteParameter : CommandParameter<Byte> {
    override fun parse(input: String): Byte {
        try {
            return input.toByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into byte for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<Byte>): String = "byte"
}