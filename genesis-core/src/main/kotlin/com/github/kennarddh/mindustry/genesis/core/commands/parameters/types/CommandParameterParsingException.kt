package com.github.kennarddh.mindustry.genesis.core.commands.parameters.types

class CommandParameterParsingException(message: String) : Exception(message) {
    fun toParametrizedString(parameterName: String): String {
        return message!!.replace(":parameterName:", parameterName)
    }
}