package com.github.kennarddh.mindustry.genesis.core.commands.parameters.types

import kotlin.reflect.KClass

interface CommandParameter<T : Any> {
    suspend fun parse(instance: KClass<T>, input: String): T

    suspend fun toUsageType(input: KClass<T>): String
}