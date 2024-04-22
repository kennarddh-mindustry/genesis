package com.github.kennarddh.mindustry.genesis.core.commands.senders

import arc.util.Log
import kotlin.reflect.full.createType

class ServerCommandSender : CommandSender() {
    companion object {
        val type = ServerCommandSender::class.createType()
    }

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
