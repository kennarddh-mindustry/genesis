package com.github.kennarddh.mindustry.genesis.core.commons

import kotlinx.coroutines.runBlocking
import mindustry.mod.Plugin

abstract class AbstractPlugin : Plugin() {
    @Deprecated("Plugin only. This is for mod.", ReplaceWith("Plugin only. This is for mod."), DeprecationLevel.HIDDEN)
    final override fun loadContent() = Unit

    final override fun init() = runBlocking { onInit() }

    /**
     * Called when the Application is destroyed.
     *
     * Will be run at the same time for every plugin's dispose including Handler onDispose.
     */
    open suspend fun dispose() = Unit

    /**
     * Will be fired at genesis plugin init and at the same time with every AbstractPlugin asyncInit.
     */
    open suspend fun onAsyncInit() = Unit

    /**
     * Will be fired at genesis plugin init.
     */
    open suspend fun onGenesisInit() = Unit

    /**
     * Will be fired at genesis plugin init and at the same time with every AbstractPlugin.
     */
    open suspend fun onInit() = Unit
}