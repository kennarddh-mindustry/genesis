package kennarddh.genesis

import arc.util.Log
import kennarddh.genesis.commands.CommandRegistry
import mindustry.mod.Plugin

@SuppressWarnings("unused")
class Genesis : Plugin() {

    override fun init() {
        Log.info("[Genesis] Loaded")

        val commandRegistry = CommandRegistry()

        commandRegistry.init()
    }
}