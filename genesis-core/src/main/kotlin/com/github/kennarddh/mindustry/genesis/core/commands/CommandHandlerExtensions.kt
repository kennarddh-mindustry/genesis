package com.github.kennarddh.mindustry.genesis.core.commands

import arc.struct.ObjectMap
import arc.struct.Seq
import arc.util.CommandHandler
import arc.util.CommandHandler.Command
import java.util.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

fun CommandHandler.registerArcCommand(arcCommand: ArcCommand) {
    val orderedCommandsField = CommandHandler::class.declaredMemberProperties.find { it.name == "orderedCommands" }
        ?: throw Error("orderedCommands doesn't exist in CommandHandler")

    orderedCommandsField.isAccessible = true

    val commandsField = CommandHandler::class.declaredMemberProperties.find { it.name == "commands" }
        ?: throw Error("commands doesn't exist in CommandHandler")

    commandsField.isAccessible = true

    @Suppress("UNCHECKED_CAST")
    val orderedCommands = orderedCommandsField.get(this) as Seq<Command>

    @Suppress("UNCHECKED_CAST")
    val commands = commandsField.get(this) as ObjectMap<String, Command>

    orderedCommands.remove { it.text == arcCommand.name }

    commands.put(arcCommand.name.lowercase(Locale.getDefault()), arcCommand)
    orderedCommands.add(arcCommand)
}