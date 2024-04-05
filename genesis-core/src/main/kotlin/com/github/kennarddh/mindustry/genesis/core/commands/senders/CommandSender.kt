package com.github.kennarddh.mindustry.genesis.core.commands.senders

abstract class CommandSender {
    abstract fun sendMessage(string: String)

    abstract fun sendSuccess(string: String)

    abstract fun sendError(string: String)
}
