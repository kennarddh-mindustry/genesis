package com.github.kennarddh.mindustry.toast.core.commands.paramaters.types

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import mindustry.Vars
import mindustry.type.Item
import kotlin.reflect.KClass

class ItemParameter : CommandParameter<Item> {
    override suspend fun parse(instance: KClass<Item>, input: String): Item {
        return Vars.content.items().toArray().find { it.name.equals(input, ignoreCase = true) }
            ?: throw CommandParameterParsingException("Cannot convert $input into item for parameter :parameterName:.")
    }

    override suspend fun toUsageType(input: KClass<Item>): String = "item"
}