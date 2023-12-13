package kennarddh.genesis.core.commands.parameters.types.base

class CommandParameterParsingException(message: String) : Exception(message) {
    fun toParametrizedString(parameterName: String): String {
        return message!!.replace(":parameterName:", parameterName)
    }
}