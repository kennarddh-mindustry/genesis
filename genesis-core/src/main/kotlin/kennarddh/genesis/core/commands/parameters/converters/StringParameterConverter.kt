package kennarddh.genesis.core.commands.parameters.converters

import kennarddh.genesis.core.commands.parameters.converters.base.CommandParameterConverter

class StringParameterConverter : CommandParameterConverter<String> {
    override fun parse(input: String): String {
        return input
    }
}