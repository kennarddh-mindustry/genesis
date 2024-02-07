package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.floating

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class FloatParameter : CommandParameter<Float> {
    override suspend fun parse(instance: KClass<Float>, input: String): Float {
        try {
            return input.toFloat()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into float for parameter :parameterName:.")
        }
    }

    override suspend fun toUsageType(input: KClass<Float>): String = "float"
}