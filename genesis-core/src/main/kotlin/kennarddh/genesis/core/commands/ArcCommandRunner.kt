package kennarddh.genesis.core.commands

import arc.util.CommandHandler.CommandRunner
import mindustry.gen.Player

class ArcCommandRunner(private val commandRegistry: CommandRegistry, val name: String) : CommandRunner<Player> {
    override fun accept(args: Array<out String>?, player: Player?) {
        val parametersString = args?.get(0) ?: ""

        commandRegistry.invokeCommand(name, parametersString, player)
    }
}