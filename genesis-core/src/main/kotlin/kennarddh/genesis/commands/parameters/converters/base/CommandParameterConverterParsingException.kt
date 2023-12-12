package kennarddh.genesis.commands.parameters.converters.base

class CommandParameterConverterParsingException(message: String) : Exception(message) {
    fun toParametrizedString(parameterName: String): String {
        return message!!.replace(":parameterName:", parameterName)
    }
}