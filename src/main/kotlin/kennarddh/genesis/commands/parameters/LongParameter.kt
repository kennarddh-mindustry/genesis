package kennarddh.genesis.commands.parameters

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class LongParameter : CommandParameter<Long> {
    override fun parse(input: String): Long {
        try {
            return input.toLong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into long for parameter :parameterName:.")
        }
    }
}