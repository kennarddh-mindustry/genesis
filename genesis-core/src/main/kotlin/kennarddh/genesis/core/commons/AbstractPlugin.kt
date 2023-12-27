package kennarddh.genesis.core.commons

import mindustry.mod.Plugin

abstract class AbstractPlugin : Plugin() {
    @Deprecated("Useless", ReplaceWith("Plugin only. This is for mod."), DeprecationLevel.HIDDEN)
    final override fun loadContent() = Unit
}