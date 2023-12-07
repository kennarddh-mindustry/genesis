package kennarddh.genesis.commands.parameters

import kennarddh.genesis.commands.parameters.base.CommandParameter
import kennarddh.genesis.commands.parameters.base.CommandParameterParsingException

class BooleanParameter : CommandParameter<Boolean> {
    override fun parse(input: String): Boolean {
        return if (input.lowercase() == "on" || input.lowercase() == "true" || input.lowercase() == "yes") true
        else if (input.lowercase() == "off" || input.lowercase() == "false" || input.lowercase() == "no") false
        else throw CommandParameterParsingException("Cannot convert $input into boolean for parameter :parameterName:.")
    }
}