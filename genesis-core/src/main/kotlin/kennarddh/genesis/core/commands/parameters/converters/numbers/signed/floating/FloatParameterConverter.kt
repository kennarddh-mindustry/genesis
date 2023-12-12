package kennarddh.genesis.core.commands.parameters.converters.numbers.signed.floating

import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverterParsingException

class FloatParameterConverter : CommandParameterConverter<Float> {
    override fun parse(input: String): Float {
        try {
            return input.toFloat()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into float for parameter :parameterName:.")
        }
    }
}