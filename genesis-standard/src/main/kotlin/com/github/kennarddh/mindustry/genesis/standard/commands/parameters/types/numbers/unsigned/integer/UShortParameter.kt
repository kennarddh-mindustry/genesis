package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class UShortParameter : CommandParameter<UShort> {
    override fun parse(instance: KClass<UShort>, input: String): UShort {
        try {
            return input.toUShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned short for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<UShort>): String = "uShort"
}