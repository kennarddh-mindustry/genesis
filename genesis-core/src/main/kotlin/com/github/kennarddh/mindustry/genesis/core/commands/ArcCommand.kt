package com.github.kennarddh.mindustry.genesis.core.commands

import arc.util.CommandHandler

/**
 * [name] must not contains upper case letter
 */
class ArcCommand(
    val commandRegistry: CommandRegistry,
    val name: String,
    description: String,
    val brief: String,
    val aliasFor: String?
) : CommandHandler.Command(name, "[params...]", description, ArcCommandRunner(commandRegistry, name)) {
    val isAlias: Boolean
        get() = aliasFor != null

    val commandData: CommandData
        get() = commandRegistry.getCommandFromCommandName(name)!!

    val realName: String
        get() = if (isAlias) aliasFor!! else name

    suspend fun toUsage() = commandData.toUsage()
}