package kennarddh.genesis.core.commands

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

    @Deprecated("Do not use this with genesis", ReplaceWith("Genesis command annotation"), DeprecationLevel.HIDDEN)
    override fun removeCommand(text: String?) {
        throw UnsupportedOperationException("Do not use this with genesis. Replace it with Genesis command annotation. If you are not the mod creator, This mod can't be used with Genesis because Genesis override mindustry command system.")
    }

    @Deprecated("Do not use this with genesis", ReplaceWith("Genesis command annotation"), DeprecationLevel.HIDDEN)
    override fun <T : Any?> register(
        text: String?,
        params: String?,
        description: String?,
        runner: CommandRunner<T>?
    ): Command {
        throw UnsupportedOperationException("Do not use this with genesis. Replace it with Genesis command annotation. If you are not the mod creator, This mod can't be used with Genesis because Genesis override mindustry command system.")
    }
}
