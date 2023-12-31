package kennarddh.genesis.common.commands.parameters.types.numbers.unsigned.integer

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class UIntParameter : CommandParameter<UInt> {
    override fun parse(instance: KClass<UInt>, input: String): UInt {
        try {
            return input.toUInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned int for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<UInt>): String = "uInt"
}