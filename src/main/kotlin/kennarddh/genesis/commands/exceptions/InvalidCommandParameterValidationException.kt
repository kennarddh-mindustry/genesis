package kennarddh.genesis.commands.exceptions

class CommandParameterValidationException(val messages: Array<String>) : Exception() {
    override val message: String
        get() = messages.joinToString("\n")
}