package kennarddh.genesis.core.commands.parameters.types.numbers.unsigned.integer

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException
import kotlin.reflect.KClass

class UIntParameter : CommandParameter<UInt> {
    override fun parse(input: String): UInt {
        try {
            return input.toUInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned int for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<UInt>): String = "uInt"
}