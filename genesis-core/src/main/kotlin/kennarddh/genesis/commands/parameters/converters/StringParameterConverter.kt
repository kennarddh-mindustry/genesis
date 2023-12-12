package kennarddh.genesis.commands.parameters.converters

import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverter

class StringParameterConverter : CommandParameterConverter<String> {
    override fun parse(input: String): String {
        return input
    }
}