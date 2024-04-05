package com.github.kennarddh.mindustry.genesis.core.commands.senders

import arc.util.Log

class ServerCommandSender : CommandSender() {
    override fun sendMessage(string: String) {
        Log.info(string)
    }

    override fun sendSuccess(string: String) {
        Log.info(string)
    }

    override fun sendError(string: String) {
        Log.err(string)
    }
}
