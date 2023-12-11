package kennarddh.genesis.common

import arc.util.CommandHandler
import mindustry.mod.Plugin

abstract class AbstractPlugin : Plugin() {
    @Deprecated("This won't add command.", ReplaceWith("CommandRegistry"), DeprecationLevel.HIDDEN)
    final override fun registerClientCommands(handler: CommandHandler?) = Unit

    @Deprecated("This won't add command.", ReplaceWith("CommandRegistry"), DeprecationLevel.HIDDEN)
    final override fun registerServerCommands(handler: CommandHandler?) = Unit

    @Deprecated("DO NOT USE OR CALL THIS", ReplaceWith("Plugin only. This is for mod."), DeprecationLevel.HIDDEN)
    final override fun loadContent() = Unit
}