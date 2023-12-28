package kennarddh.genesis.core.commands

import arc.util.CommandHandler

class ArcCommand(
    val commandRegistry: CommandRegistry,
    val name: String,
    val description: String,
    val brief: String,
    val aliasFor: String?
) : CommandHandler.Command(name, "[params...]", description, ArcCommandRunner(commandRegistry, name)) {
    val isAlias: Boolean
        get() = aliasFor != null

    val usage: String
        get() = commandRegistry.getCommandFromCommandName(name)!!.toUsage()
}