package kennarddh.genesis.commands

import arc.util.CommandHandler
import mindustry.gen.Player

val DummyValidCommandResponse = CommandHandler.CommandResponse(
    CommandHandler.ResponseType.valid,
    CommandHandler.Command("", "", "") { _, _ -> },
    ""
)

class InterceptedCommandHandler(prefix: String?, private val onIntercept: (String, Player?) -> Unit) :
    CommandHandler(prefix) {
    override fun handleMessage(commandWithPrefix: String, params: Any?): CommandResponse {
        if (!commandWithPrefix.startsWith(prefix))
            return DummyValidCommandResponse

        val command = commandWithPrefix.substring(prefix.length)

        onIntercept(command, if (params is Player) params else null)

        return DummyValidCommandResponse
    }
}
