package kennarddh.genesis.core.commands.parameters.types

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException

class CharParameter : CommandParameter<Char> {
    override fun parse(input: String): Char {
        val charArray = input.toCharArray()

        if (charArray.size == 1) return charArray[0]

        throw CommandParameterParsingException("Cannot convert $input into char for parameter :parameterName:.")
    }
}