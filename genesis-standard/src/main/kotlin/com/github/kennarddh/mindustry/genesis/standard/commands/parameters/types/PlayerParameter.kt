package com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameter
import com.github.kennarddh.mindustry.genesis.core.commands.parameters.types.CommandParameterParsingException
import mindustry.gen.Groups
import mindustry.gen.Player
import kotlin.reflect.KClass

class PlayerParameter : CommandParameter<Player> {
    override fun parse(instance: KClass<Player>, input: String): Player {
        return try {
            Groups.player.find { it.id == input.toInt() }
        } catch (error: NumberFormatException) {
            Groups.player.find { it.name == input }
                ?: throw CommandParameterParsingException("Cannot convert $input into player for parameter :parameterName:. Either it's not a valid player name or player id.")
        }
    }

    override fun toUsageType(input: KClass<Player>): String = "player"
}