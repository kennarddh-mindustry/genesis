package kennarddh.genesis.core.commands

import arc.util.CommandHandler

class ArcCommand(
    val commandRegistry: CommandRegistry,
    val name: String,
    description: String,
    val brief: String,
    val aliasFor: String?
) : CommandHandler.Command(name, "[params...]", description, ArcCommandRunner(commandRegistry, name)) {
    val isAlias: Boolean
        get() = aliasFor != null

    val usage: String
        get() = commandData.toUsage()

    val commandData: CommandData
        get() = commandRegistry.getCommandFromCommandName(name)!!

    val realName: String
        get() = if (isAlias) aliasFor!! else name
}