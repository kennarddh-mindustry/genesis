package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class ByteParameter : CommandParameter<Byte> {
    override suspend fun parse(instance: KClass<Byte>, input: String): Byte {
        try {
            return input.toByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into byte for parameter :parameterName:.")
        }
    }

    override suspend fun toUsageType(input: KClass<Byte>): String = "byte"
}