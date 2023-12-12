package kennarddh.genesis.core.commands.parameters.converters.numbers.signed.floating

import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverterParsingException

class DoubleParameterConverter : CommandParameterConverter<Double> {
    override fun parse(input: String): Double {
        try {
            return input.toDouble()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into double for parameter :parameterName:.")
        }
    }
}