package kennarddh.genesis.core.commands

import arc.util.CommandHandler.CommandRunner
import mindustry.gen.Player

class ArcCommandRunner(val commandRegistry: CommandRegistry, val name: String) : CommandRunner<Player> {
    override fun accept(args: Array<out String>?, parameter: Player?) {
        println("name|: $name| args: ${args.contentToString()}| player: $parameter")
    }
}