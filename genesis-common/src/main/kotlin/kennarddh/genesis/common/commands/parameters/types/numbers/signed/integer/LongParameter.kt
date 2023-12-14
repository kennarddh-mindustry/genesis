package kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class LongParameter : CommandParameter<Long> {
    override fun parse(input: String): Long {
        try {
            return input.toLong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into long for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<Long>): String = "long"
}