package kennarddh.genesis.commands.parameters

import kennarddh.genesis.commands.parameters.base.CommandParameter

class StringParameter : CommandParameter<String> {
    override fun parse(input: String): String {
        return input
    }
}