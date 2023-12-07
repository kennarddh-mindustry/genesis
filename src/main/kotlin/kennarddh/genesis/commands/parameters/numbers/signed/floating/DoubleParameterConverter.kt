package kennarddh.genesis.commands.parameters.numbers.signed.floating

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException

class DoubleParameterConverter : CommandParameterConverter<Double> {
    override fun parse(input: String): Double {
        try {
            return input.toDouble()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into double for parameter :parameterName:.")
        }
    }
}