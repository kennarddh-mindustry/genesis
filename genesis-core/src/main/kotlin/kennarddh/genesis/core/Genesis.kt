package kennarddh.genesis.core

import arc.util.Log
import kennarddh.genesis.core.commands.CommandRegistry
import kennarddh.genesis.core.common.AbstractPlugin
import kennarddh.genesis.core.events.EventRegistry
import kennarddh.genesis.core.handlers.Handler

@Suppress("unused")
object Genesis : AbstractPlugin() {
    private val handlers: MutableList<Handler> = mutableListOf()
    private val commandRegistry = CommandRegistry()
    private val eventSystem = EventRegistry()

    override fun init() {
        commandRegistry.init()
        eventSystem.init()

        Log.info("[GenesisCore] Loaded")
    }

    fun addHandler(handler: Handler) {
        handlers.add(handler)

        handler.onInit()

        commandRegistry.registerHandler(handler)
        eventSystem.registerHandler(handler)
    }
}