package kennarddh.genesis.commands.parameters.converters

import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverterParsingException

class CharParameterConverter : CommandParameterConverter<Char> {
    override fun parse(input: String): Char {
        val charArray = input.toCharArray()

        if (charArray.size == 1) return charArray[0]

        throw CommandParameterConverterParsingException("Cannot convert $input into char for parameter :parameterName:.")
    }
}