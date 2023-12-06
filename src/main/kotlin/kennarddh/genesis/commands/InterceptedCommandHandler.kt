package kennarddh.genesis.commands

import arc.util.CommandHandler
import mindustry.gen.Player

val dummyCommand = CommandHandler.Command("", "", "") { _, _ -> }

val dummyValidCommandResponse = CommandHandler.CommandResponse(
    CommandHandler.ResponseType.valid,
    dummyCommand,
    ""
)

val dummyNoCommandResponse = CommandHandler.CommandResponse(
    CommandHandler.ResponseType.noCommand,
    dummyCommand,
    ""
)

class InterceptedCommandHandler(prefix: String?, private val onIntercept: (String, Player?) -> Unit) :
    CommandHandler(prefix) {
    override fun handleMessage(commandWithPrefix: String, params: Any?): CommandResponse {
        if (!commandWithPrefix.startsWith(prefix))
            return dummyNoCommandResponse

        val command = commandWithPrefix.substring(prefix.length)

        onIntercept(command, if (params is Player) params else null)

        return dummyValidCommandResponse
    }
}
