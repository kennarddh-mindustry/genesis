package kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class IntParameter : CommandParameter<Int> {
    override fun parse(instance: KClass<Int>, input: String): Int {
        try {
            return input.toInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into int for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<Int>): String = "int"
}