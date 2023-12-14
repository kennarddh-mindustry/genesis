package kennarddh.genesis.common.commands.parameters.types.numbers.unsigned.integer

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class ULongParameter : CommandParameter<ULong> {
    override fun parse(input: String): ULong {
        try {
            return input.toULong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned long for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<ULong>): String = "uLong"
}