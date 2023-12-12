package kennarddh.genesis.core.commands.parameters.converters.numbers.signed.integer

import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverterParsingException

class LongParameterConverter : CommandParameterConverter<Long> {
    override fun parse(input: String): Long {
        try {
            return input.toLong()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into long for parameter :parameterName:.")
        }
    }
}