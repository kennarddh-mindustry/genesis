package kennarddh.genesis.core.commands.parameters.converters.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverterParsingException

class IntParameterConverter : CommandParameterConverter<Int> {
    override fun parse(input: String): Int {
        try {
            return input.toInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into int for parameter :parameterName:.")
        }
    }
}