package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import kotlin.reflect.KClass

class StringParameter : CommandParameter<String> {
    override fun parse(instance: KClass<String>, input: String): String {
        return input
    }

    override fun toUsageType(input: KClass<String>): String = "string"
}