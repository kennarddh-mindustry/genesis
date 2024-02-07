package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class UIntParameter : CommandParameter<UInt> {
    override suspend fun parse(instance: KClass<UInt>, input: String): UInt {
        try {
            return input.toUInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned int for parameter :parameterName:.")
        }
    }

    override suspend fun toUsageType(input: KClass<UInt>): String = "uInt"
}