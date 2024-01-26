package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class ShortParameter : CommandParameter<Short> {
    override fun parse(instance: KClass<Short>, input: String): Short {
        try {
            return input.toShort()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into short for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<Short>): String = "short"
}