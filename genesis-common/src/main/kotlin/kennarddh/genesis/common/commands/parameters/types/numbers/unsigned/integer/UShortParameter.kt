package kennarddh.genesis.common.commands.parameters.types.numbers.unsigned.integer

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class UShortParameter : CommandParameter<UShort> {
    override fun parse(input: String): UShort {
        try {
            return input.toUShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned short for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<UShort>): String = "uShort"
}