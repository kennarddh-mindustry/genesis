package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass
import kotlin.time.Duration

class DurationParameter : CommandParameter<Duration> {
    override fun parse(instance: KClass<Duration>, input: String): Duration {
        return Duration.parseOrNull(input)
            ?: throw CommandParameterParsingException("Cannot convert $input into duration for parameter :parameterName:.")
    }

    override fun toUsageType(input: KClass<Duration>): String = "duration"
}