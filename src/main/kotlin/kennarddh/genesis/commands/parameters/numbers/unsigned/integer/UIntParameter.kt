package kennarddh.genesis.commands.parameters.numbers.unsigned.integer

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class UIntParameter : CommandParameter<UInt> {
    override fun parse(input: String): UInt {
        try {
            return input.toUInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned int for parameter :parameterName:.")
        }
    }
}