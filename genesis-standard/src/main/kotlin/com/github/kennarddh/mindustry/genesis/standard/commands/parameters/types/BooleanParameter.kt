package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class BooleanParameter : CommandParameter<Boolean> {
    override suspend fun parse(instance: KClass<Boolean>, input: String): Boolean {
        return if (input.lowercase() == "on" || input.lowercase() == "true" || input.lowercase() == "yes" || input.lowercase() == "y") true
        else if (input.lowercase() == "off" || input.lowercase() == "false" || input.lowercase() == "no" || input.lowercase() == "n") false
        else throw CommandParameterParsingException("Cannot convert $input into boolean for parameter :parameterName:.")
    }

    override suspend fun toUsageType(input: KClass<Boolean>): String = "boolean"
}