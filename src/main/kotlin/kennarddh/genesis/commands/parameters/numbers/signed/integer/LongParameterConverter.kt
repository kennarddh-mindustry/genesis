package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException

class LongParameterConverter : CommandParameterConverter<Long> {
    override fun parse(input: String): Long {
        try {
            return input.toLong()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into long for parameter :parameterName:.")
        }
    }
}