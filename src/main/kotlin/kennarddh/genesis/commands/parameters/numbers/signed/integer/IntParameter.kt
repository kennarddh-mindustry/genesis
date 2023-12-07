package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class IntParameter : CommandParameter<Int> {
    override fun parse(input: String): Int {
        try {
            return input.toInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into int for parameter :parameterName:.")
        }
    }
}