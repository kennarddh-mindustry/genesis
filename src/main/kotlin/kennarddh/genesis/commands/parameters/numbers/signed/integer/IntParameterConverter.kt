package kennarddh.genesis.commands.parameters.numbers.signed.integer

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.base.CommandParameterConverterParsingException

class IntParameterConverter : CommandParameterConverter<Int> {
    override fun parse(input: String): Int {
        try {
            return input.toInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into int for parameter :parameterName:.")
        }
    }
}