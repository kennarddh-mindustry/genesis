package kennarddh.genesis.commands.parameters.numbers.signed.floating

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException

class FloatParameterConverter : CommandParameterConverter<Float> {
    override fun parse(input: String): Float {
        try {
            return input.toFloat()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into float for parameter :parameterName:.")
        }
    }
}