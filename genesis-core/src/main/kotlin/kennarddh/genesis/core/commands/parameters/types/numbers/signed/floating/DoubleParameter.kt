package kennarddh.genesis.core.commands.parameters.types.numbers.signed.floating

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class DoubleParameter : CommandParameter<Double> {
    override fun parse(input: String): Double {
        try {
            return input.toDouble()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into double for parameter :parameterName:.")
        }
    }
}