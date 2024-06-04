package com.github.kennarddh.mindustry.genesis.standard.handlers.commands

import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.*
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.floating.DoubleParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.floating.FloatParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer.ByteParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer.IntParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer.LongParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.signed.integer.ShortParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer.UByteParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer.UIntParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer.ULongParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.types.numbers.unsigned.integer.UShortParameter
import com.github.kennarddh.mindustry.genesis.standard.commands.parameters.validations.numbers.*
import com.github.kennarddh.mindustry.toast.core.commands.paramaters.types.ItemParameter
import com.github.kennarddh.mindustry.toast.core.commands.paramaters.types.TeamParameter
import com.github.kennarddh.mindustry.toast.core.commands.paramaters.types.UnitTypeParameter
import mindustry.game.Team
import mindustry.gen.Player
import mindustry.type.Item
import mindustry.type.UnitType
import kotlin.time.Duration

class CommandsHandler : Handler {
    override suspend fun onInit() {
        Genesis.commandRegistry.registerParameterType(Boolean::class, BooleanParameter())
        Genesis.commandRegistry.registerParameterType(Char::class, CharParameter())
        Genesis.commandRegistry.registerParameterType(String::class, StringParameter())

        Genesis.commandRegistry.registerParameterType(Float::class, FloatParameter())
        Genesis.commandRegistry.registerParameterType(Double::class, DoubleParameter())

        Genesis.commandRegistry.registerParameterType(Byte::class, ByteParameter())
        Genesis.commandRegistry.registerParameterType(Short::class, ShortParameter())
        Genesis.commandRegistry.registerParameterType(Int::class, IntParameter())
        Genesis.commandRegistry.registerParameterType(Long::class, LongParameter())

        Genesis.commandRegistry.registerParameterType(UByte::class, UByteParameter())
        Genesis.commandRegistry.registerParameterType(UShort::class, UShortParameter())
        Genesis.commandRegistry.registerParameterType(UInt::class, UIntParameter())
        Genesis.commandRegistry.registerParameterType(ULong::class, ULongParameter())

        Genesis.commandRegistry.registerParameterType(Enum::class, EnumParameter())

        Genesis.commandRegistry.registerParameterType(Duration::class, DurationParameter())
        Genesis.commandRegistry.registerParameterType(Player::class, PlayerParameter())
        Genesis.commandRegistry.registerParameterType(UnitType::class, UnitTypeParameter())
        Genesis.commandRegistry.registerParameterType(Team::class, TeamParameter())
        Genesis.commandRegistry.registerParameterType(Item::class, ItemParameter())

        Genesis.commandRegistry.registerParameterValidationAnnotation(
            GT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGT
        )
        Genesis.commandRegistry.registerParameterValidationAnnotation(
            GTE::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGTE
        )
        Genesis.commandRegistry.registerParameterValidationAnnotation(
            LT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateLT
        )
        Genesis.commandRegistry.registerParameterValidationAnnotation(
            LTE::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateLTE
        )
    }
}