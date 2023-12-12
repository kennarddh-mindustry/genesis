package kennarddh.genesis.core.commands.parameters.converters

import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverterParsingException

class BooleanParameterConverter : CommandParameterConverter<Boolean> {
    override fun parse(input: String): Boolean {
        return if (input.lowercase() == "on" || input.lowercase() == "true" || input.lowercase() == "yes") true
        else if (input.lowercase() == "off" || input.lowercase() == "false" || input.lowercase() == "no") false
        else throw CommandParameterConverterParsingException("Cannot convert $input into boolean for parameter :parameterName:.")
    }
}