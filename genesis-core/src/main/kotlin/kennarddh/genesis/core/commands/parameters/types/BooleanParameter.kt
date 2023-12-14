package kennarddh.genesis.core.commands.parameters.types

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.base.CommandParameterParsingException
import kotlin.reflect.KClass

class BooleanParameter : CommandParameter<Boolean> {
    override fun parse(input: String): Boolean {
        return if (input.lowercase() == "on" || input.lowercase() == "true" || input.lowercase() == "yes") true
        else if (input.lowercase() == "off" || input.lowercase() == "false" || input.lowercase() == "no") false
        else throw CommandParameterParsingException("Cannot convert $input into boolean for parameter :parameterName:.")
    }

    override fun toUsageType(input: KClass<Boolean>): String = "boolean"
}