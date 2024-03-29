package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class EnumParameter<T : Enum<*>> : CommandParameter<T> {
    override suspend fun parse(instance: KClass<T>, input: String): T {
        // TODO: https://youtrack.jetbrains.com/issue/KT-14743
        return instance.java.enumConstants.find { it.name == input }
            ?: throw CommandParameterParsingException("$input is not a valid value for :parameterName: parameter enum.")
    }

    override suspend fun toUsageType(input: KClass<T>): String {
        val validValues = input.java.enumConstants.map { it.name }

        return validValues.joinToString("|")
    }
}