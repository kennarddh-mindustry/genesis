package kennarddh.genesis.commands.parameters.converters.numbers.signed.floating

import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverterParsingException

class DoubleParameterConverter : CommandParameterConverter<Double> {
    override fun parse(input: String): Double {
        try {
            return input.toDouble()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into double for parameter :parameterName:.")
        }
    }
}