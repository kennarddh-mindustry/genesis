package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class UByteParameter : CommandParameter<UByte> {
    override fun parse(instance: KClass<UByte>, input: String): UByte {
        try {
            return input.toUByte()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into unsigned byte for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<UByte>): String = "uByte"
}