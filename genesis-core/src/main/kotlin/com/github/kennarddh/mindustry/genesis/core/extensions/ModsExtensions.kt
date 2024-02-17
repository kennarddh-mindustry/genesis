package com.github.kennarddh.mindustry.genesis.core.extensions

import com.github.kennarddh.mindustry.genesis.core.commons.AbstractPlugin
import mindustry.Vars
import mindustry.mod.Mod
import mindustry.mod.Mods
import mindustry.mod.Mods.LoadedMod

inline fun <reified T : Mod> Mods.getLoadedMod(): LoadedMod? = Vars.mods.getMod(T::class.java)

inline fun <reified T : Mod> Mods.getLoadedMods(): List<LoadedMod> =
    orderedMods().toList().filter { it.main is T }

inline fun <reified T : Mod> Mods.getLoadedEnabledMods(): List<LoadedMod> =
    getLoadedMods<T>().filter { it.enabled() }

inline fun <reified T : Mod> Mods.getLoadedJavaMods(): List<LoadedMod> =
    getLoadedMods<T>().filter { it.isJava }

inline fun <reified T : Mod> Mods.getEnabledJavaLoadedMods(): List<LoadedMod> =
    getLoadedEnabledMods<T>().filter { it.isJava }

inline fun <reified T : Mod> Mods.getMod(): Mod? = getLoadedMod<T>()?.main

inline fun <reified T : Mod> Mods.getLoadedJavaMod(): Mod? {
    val loadedMod = getLoadedMod<T>() ?: return null

    if (!loadedMod.isJava)
        return null

    return loadedMod.main
}

fun Mods.getLoadedAbstractPluginsOrdered(): List<LoadedMod> = getLoadedJavaMods<AbstractPlugin>()

fun Mods.getLoadedEnabledAbstractPluginsOrdered(): List<LoadedMod> =
    getEnabledJavaLoadedMods<AbstractPlugin>()

fun Mods.getEnabledAbstractPluginsOrdered(): List<AbstractPlugin> =
    getEnabledJavaLoadedMods<AbstractPlugin>().map { it.main as AbstractPlugin }
