package kennarddh.genesis.core

import arc.util.Log
import kennarddh.genesis.core.commands.CommandRegistry
import kennarddh.genesis.core.commons.AbstractPlugin
import kennarddh.genesis.core.events.EventRegistry
import kennarddh.genesis.core.handlers.Handler
import kennarddh.genesis.core.packets.PacketRegistry

class Genesis : AbstractPlugin() {
    override fun init() {
        commandRegistry.init()
        eventRegistry.init()
        packetRegistry.init()

        Log.info("[GenesisCore] Loaded")
    }

    companion object {
        private val handlers: MutableList<Handler> = mutableListOf()
        val commandRegistry = CommandRegistry()
        private val eventRegistry = EventRegistry()
        private val packetRegistry = PacketRegistry()

        fun addHandler(handler: Handler) {
            handlers.add(handler)

            handler.onInit()

            commandRegistry.registerHandler(handler)
            eventRegistry.registerHandler(handler)
            packetRegistry.registerHandler(handler)
        }
    }
}