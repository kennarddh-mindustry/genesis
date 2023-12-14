package kennarddh.genesis.common.commands.parameters.types

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class CharParameter : CommandParameter<Char> {
    override fun parse(input: String): Char {
        val charArray = input.toCharArray()

        if (charArray.size == 1) return charArray[0]

        throw CommandParameterParsingException("Cannot convert $input into char for parameter :parameterName:.")
    }

    override fun toUsageType(input: KClass<Char>): String = "char"
}