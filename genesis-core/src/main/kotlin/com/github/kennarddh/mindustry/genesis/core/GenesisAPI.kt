package com.github.kennarddh.mindustry.genesis.core

import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import mindustry.Vars

object GenesisAPI {
    val genesis: Genesis by lazy { Vars.mods.getMod(Genesis::class.java).main as Genesis }

    inline fun <reified T : Handler> getHandler(): T? = genesis.handlers.find { it is T } as T?

    inline fun <reified T : Handler> getHandlers(): List<T> = genesis.handlers.filterIsInstance<T>()

    suspend fun registerHandler(handler: Handler) = genesis.registerHandler(handler)

    val commandRegistry
        get() = genesis.commandRegistry
}