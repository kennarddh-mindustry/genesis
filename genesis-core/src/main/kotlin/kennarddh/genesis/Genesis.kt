package kennarddh.genesis

import arc.util.Log
import kennarddh.genesis.commands.CommandRegistry
import kennarddh.genesis.common.AbstractPlugin
import kennarddh.genesis.events.EventRegistry
import kennarddh.genesis.handlers.Handler
import kennarddh.genesis.handlers.server.ServerHandler

@Suppress("unused")
class Genesis : AbstractPlugin() {
    private val handlers: MutableList<Handler> = mutableListOf()
    private val commandRegistry = CommandRegistry()
    private val eventSystem = EventRegistry()

    override fun init() {
        commandRegistry.init()
        eventSystem.init()

        addHandler(ServerHandler())

        Log.info("[GenesisCore] Loaded")
    }

    fun addHandler(handler: Handler) {
        handlers.add(handler)

        handler.onInit()

        commandRegistry.registerHandler(handler)
        eventSystem.registerHandler(handler)
    }
}