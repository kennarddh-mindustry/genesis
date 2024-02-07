package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class LongParameter : CommandParameter<Long> {
    override suspend fun parse(instance: KClass<Long>, input: String): Long {
        try {
            return input.toLong()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into long for parameter :parameterName:.")
        }
    }

    override suspend fun toUsageType(input: KClass<Long>): String = "long"
}