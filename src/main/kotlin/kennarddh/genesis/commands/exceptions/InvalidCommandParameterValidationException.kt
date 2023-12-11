package kennarddh.genesis.commands.exceptions

class CommandParameterValidationException(private val messages: Array<String>) : Exception() {
    override val message: String
        get() = messages.joinToString("\n")
}