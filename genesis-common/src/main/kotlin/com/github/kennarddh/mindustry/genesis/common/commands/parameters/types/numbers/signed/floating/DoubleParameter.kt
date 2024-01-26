package com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.signed.floating

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class DoubleParameter : CommandParameter<Double> {
    override fun parse(instance: KClass<Double>, input: String): Double {
        try {
            return input.toDouble()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into double for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<Double>): String = "double"
}