package com.github.kennarddh.mindustry.genesis.core.commands.senders

import kotlin.reflect.full.createType

abstract class CommandSender {
    companion object {
        val type = CommandSender::class.createType()
    }

    abstract fun sendMessage(string: String)

    abstract fun sendSuccess(string: String)

    abstract fun sendError(string: String)
}
