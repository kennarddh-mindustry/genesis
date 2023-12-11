package kennarddh.genesis

import arc.util.Log
import kennarddh.genesis.commands.CommandRegistry
import kennarddh.genesis.common.AbstractPlugin
import kennarddh.genesis.handlers.Handler
import kennarddh.genesis.handlers.server.ServerHandler

@Suppress("unused")
class Genesis : AbstractPlugin() {
    private val handlers: MutableList<Handler> = mutableListOf()
    private val commandRegistry = CommandRegistry()

    override fun init() {
        commandRegistry.init()

        addHandler(ServerHandler())

        Log.info("[Genesis:Core] Loaded")
    }

    fun addHandler(handler: Handler) {
        handlers.add(handler)

        handler.onInit()

        commandRegistry.registerHandler(handler)
    }
}