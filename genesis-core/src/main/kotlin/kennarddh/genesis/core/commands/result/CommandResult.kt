package kennarddh.genesis.core.commands.result


data class CommandResult(
    val response: String,
    val status: CommandResultStatus = CommandResultStatus.Success,
    /**
     * For client command
     */
    val colorDependsOnStatus: Boolean = true
) {
    companion object {
        val empty = CommandResult("", CommandResultStatus.Empty)
    }
}
