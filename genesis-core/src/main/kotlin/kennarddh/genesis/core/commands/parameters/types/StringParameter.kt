package kennarddh.genesis.core.commands.parameters.types

import kennarddh.genesis.core.commands.parameters.types.base.CommandParameter

class StringParameter : CommandParameter<String> {
    override fun parse(input: String): String {
        return input
    }

    override fun toUsageType(input: String): String = "string"
}