package com.github.kennarddh.mindustry.genesis.common.commands.parameters.types

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class BooleanParameter : CommandParameter<Boolean> {
    override fun parse(instance: KClass<Boolean>, input: String): Boolean {
        return if (input.lowercase() == "on" || input.lowercase() == "true" || input.lowercase() == "yes") true
        else if (input.lowercase() == "off" || input.lowercase() == "false" || input.lowercase() == "no") false
        else throw CommandParameterParsingException("Cannot convert $input into boolean for parameter :parameterName:.")
    }

    override fun toUsageType(input: KClass<Boolean>): String = "boolean"
}