package kennarddh.genesis.commands.parameters

import kennarddh.genesis.commands.parameters.base.CommandParameterConverter

class StringParameterConverter : CommandParameterConverter<String> {
    override fun parse(input: String): String {
        return input
    }
}