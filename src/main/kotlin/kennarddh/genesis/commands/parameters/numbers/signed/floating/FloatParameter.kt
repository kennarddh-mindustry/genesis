package kennarddh.genesis.commands.parameters.numbers.signed.floating

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class FloatParameter : CommandParameter<Float> {
    override fun parse(input: String): Float {
        try {
            return input.toFloat()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into float for parameter :parameterName:.")
        }
    }
}