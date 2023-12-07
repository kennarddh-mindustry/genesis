package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class ULongParameter : CommandParameter<ULong> {
    override fun parse(input: String): ULong {
        try {
            return input.toULong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned long for parameter :parameterName:.")
        }
    }
}