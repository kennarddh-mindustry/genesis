package com.github.kennarddh.mindustry.genesis.core.commands.senders

import mindustry.gen.Player
import kotlin.reflect.full.createType

class PlayerCommandSender(val player: Player) : CommandSender() {
    companion object {
        val type = PlayerCommandSender::class.createType()
    }

    override fun sendMessage(string: String) {
        player.sendMessage(string)
    }

    override fun sendSuccess(string: String) {
        player.sendMessage("[green]$string")
    }

    override fun sendError(string: String) {
        player.sendMessage("[scarlet]$string")
    }
}
