package kennarddh.genesis.core.commands.parameters.types.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class IntParameter : CommandParameter<Int> {
    override fun parse(input: String): Int {
        try {
            return input.toInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into int for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: Int): String = "int"
}