package com.github.kennarddh.mindustry.genesis.core.commands.senders

import mindustry.gen.Player

class PlayerCommandSender(val player: Player) : CommandSender() {
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
