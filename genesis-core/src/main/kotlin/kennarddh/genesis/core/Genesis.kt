package kennarddh.genesis.core

import arc.util.Log
import kennarddh.genesis.core.commands.CommandRegistry
import kennarddh.genesis.core.commons.AbstractPlugin
import kennarddh.genesis.core.events.EventRegistry
import kennarddh.genesis.core.filters.FiltersRegistry
import kennarddh.genesis.core.handlers.Handler
import kennarddh.genesis.core.packets.PacketRegistry
import kennarddh.genesis.core.server.packets.ServerPacketsRegistry
import kennarddh.genesis.core.timers.TimersRegistry

class Genesis : AbstractPlugin() {
    override fun init() {
        commandRegistry.init()
        eventRegistry.init()
        packetRegistry.init()
        serverPacketsRegistry.init()
        filtersRegistry.init()
        timersRegistry.init()

        Log.info("[GenesisCore] Loaded")
    }

    companion object {
        private val handlers: MutableList<Handler> = mutableListOf()

        val commandRegistry = CommandRegistry()
        private val eventRegistry = EventRegistry()
        private val packetRegistry = PacketRegistry()
        private val serverPacketsRegistry = ServerPacketsRegistry()
        private val filtersRegistry = FiltersRegistry()
        private val timersRegistry = TimersRegistry()

        fun addHandler(handler: Handler) {
            handlers.add(handler)

            handler.onInit()

            commandRegistry.registerHandler(handler)
            eventRegistry.registerHandler(handler)
            packetRegistry.registerHandler(handler)
            serverPacketsRegistry.registerHandler(handler)
            filtersRegistry.registerHandler(handler)
            timersRegistry.registerHandler(handler)
        }
    }
}