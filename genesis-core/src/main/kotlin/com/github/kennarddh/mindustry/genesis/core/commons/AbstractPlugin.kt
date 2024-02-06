package com.github.kennarddh.mindustry.genesis.core.commons

import mindustry.mod.Plugin

abstract class AbstractPlugin : Plugin() {
    @Deprecated("Plugin only. This is for mod.", ReplaceWith("Plugin only. This is for mod."), DeprecationLevel.HIDDEN)
    final override fun loadContent() = Unit

    /**
     * Called when the Application is destroyed.
     *
     * Will be run at the same time for every plugin's dispose including Handler onDispose.
     */
    open suspend fun dispose() = Unit
}