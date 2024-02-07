package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class IntParameter : CommandParameter<Int> {
    override suspend fun parse(instance: KClass<Int>, input: String): Int {
        try {
            return input.toInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into int for parameter :parameterName:.")
        }
    }

    override suspend fun toUsageType(input: KClass<Int>): String = "int"
}