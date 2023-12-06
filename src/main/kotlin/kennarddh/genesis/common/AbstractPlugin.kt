package kennarddh.genesis.common

import arc.util.CommandHandler
import mindustry.mod.Plugin

abstract class AbstractPlugin : Plugin() {
    final override fun registerClientCommands(handler: CommandHandler?) = Unit

    final override fun registerServerCommands(handler: CommandHandler?) = Unit

    final override fun loadContent() = Unit
}