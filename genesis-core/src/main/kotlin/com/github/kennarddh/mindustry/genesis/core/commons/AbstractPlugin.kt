package com.github.kennarddh.mindustry.genesis.core.commons

import kotlinx.coroutines.runBlocking
import mindustry.Vars
import mindustry.mod.Plugin

abstract class AbstractPlugin : Plugin() {
    val plugin by lazy { Vars.mods.getMod(this::class.java) }

    @Deprecated("Plugin only. This is for mod.", ReplaceWith("Plugin only. This is for mod."), DeprecationLevel.HIDDEN)
    final override fun loadContent() = Unit

    final override fun init() = runBlocking { onInit() }

    /**
     * Called when the Application is destroyed.
     *
     * Will be run at the same time for every plugin's onDispose including Handler onDispose.
     */
    open suspend fun onDispose() = Unit

    /**
     * Will be fired at genesis plugin init and at the same time with every AbstractPlugin.
     */
    open suspend fun onInit() = Unit
}