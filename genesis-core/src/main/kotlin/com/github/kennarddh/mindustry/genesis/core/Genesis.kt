package com.github.kennarddh.mindustry.genesis.core

import com.github.kennarddh.mindustry.genesis.core.commands.CommandRegistry
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import com.github.kennarddh.mindustry.genesis.core.events.EventRegistry
import com.github.kennarddh.mindustry.genesis.core.filters.FiltersRegistry
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.logging.Logger
import com.github.kennarddh.mindustry.genesis.core.packets.PacketRegistry
import com.github.kennarddh.mindustry.genesis.core.server.packets.ServerPacketsRegistry
import com.github.kennarddh.mindustry.genesis.core.timers.TimersRegistry

class Genesis : AbstractPlugin() {
    override fun init() {
        commandRegistry.init()
        eventRegistry.init()
        packetRegistry.init()
        serverPacketsRegistry.init()
        filtersRegistry.init()
        timersRegistry.init()

        Logger.info("Loaded")
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