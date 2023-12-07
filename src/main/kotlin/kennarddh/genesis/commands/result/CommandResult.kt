package kennarddh.genesis.commands.result


data class CommandResult(val response: String, val status: CommandResultStatus = CommandResultStatus.Success) {
    companion object {
        val empty = CommandResult("", CommandResultStatus.Empty)
    }
}
