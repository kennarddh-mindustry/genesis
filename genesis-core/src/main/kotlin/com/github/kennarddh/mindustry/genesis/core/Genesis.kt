package com.github.kennarddh.mindustry.genesis.core

import arc.ApplicationListener
import arc.Core
import com.github.kennarddh.mindustry.genesis.core.commands.CommandRegistry
import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import com.github.kennarddh.mindustry.genesis.core.events.EventRegistry
import com.github.kennarddh.mindustry.genesis.core.filters.FiltersRegistry
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.logging.Logger
import com.github.kennarddh.mindustry.genesis.core.packets.PacketRegistry
import com.github.kennarddh.mindustry.genesis.core.server.packets.ServerPacketsRegistry
import com.github.kennarddh.mindustry.genesis.core.timers.TimersRegistry
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mindustry.Vars

class Genesis : AbstractPlugin() {
    private val backingHandlers: MutableList<Handler> = mutableListOf()

    val handlers: List<Handler>
        get() = backingHandlers.toList()

    internal val commandRegistry = CommandRegistry()
    internal val eventRegistry = EventRegistry()
    internal val packetRegistry = PacketRegistry()
    internal val serverPacketsRegistry = ServerPacketsRegistry()
    internal val filtersRegistry = FiltersRegistry()
    internal val timersRegistry = TimersRegistry()

    override fun init() {
        commandRegistry.init()
        eventRegistry.init()
        packetRegistry.init()
        serverPacketsRegistry.init()
        filtersRegistry.init()
        timersRegistry.init()

        Core.app.addListener(object : ApplicationListener {
            override fun dispose() {
                runBlocking {
                    Logger.info("Gracefully shutting down")

                    handlers.forEach {
                        launch {
                            it.onDispose()
                        }
                    }

                    // Reversed because needs to dispose dependant mod/plugin plugin before the dependencies
                    val mods = Vars.mods.orderedMods().toList().reversed()

                    mods.forEach {
                        if (!it.isJava) return@forEach
                        if (!it.enabled()) return@forEach
                        if (it.main !is AbstractPlugin) return@forEach

                        val plugin = it.main as AbstractPlugin

                        launch {
                            plugin.dispose()
                        }
                    }
                }

                Logger.info("Stopped")
            }
        })

        Logger.info("Loaded")
    }

    internal suspend fun registerHandler(handler: Handler) {
        backingHandlers.add(handler)

        handler.onInit()

        commandRegistry.registerHandler(handler)
        eventRegistry.registerHandler(handler)
        packetRegistry.registerHandler(handler)
        serverPacketsRegistry.registerHandler(handler)
        filtersRegistry.registerHandler(handler)
        timersRegistry.registerHandler(handler)

        handler.onRegistered()
    }
}