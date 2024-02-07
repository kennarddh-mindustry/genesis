package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class ULongParameter : CommandParameter<ULong> {
    override suspend fun parse(instance: KClass<ULong>, input: String): ULong {
        try {
            return input.toULong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned long for parameter :parameterName:.")
        }
    }

    override suspend fun toUsageType(input: KClass<ULong>): String = "uLong"
}