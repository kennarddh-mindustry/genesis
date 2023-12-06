package kennarddh.genesis

import arc.util.Log
import kennarddh.genesis.commands.CommandRegistry
import kennarddh.genesis.common.AbstractPlugin

@SuppressWarnings("unused")
class Genesis : AbstractPlugin() {
    override fun init() {
        Log.info("[Genesis] Loaded")

        val commandRegistry = CommandRegistry()

        commandRegistry.init()
    }
}