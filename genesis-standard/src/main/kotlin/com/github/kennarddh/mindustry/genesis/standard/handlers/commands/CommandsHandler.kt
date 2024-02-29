package com.github.kennarddh.mindustry.genesis.standard.handlers.commands

import com.github.kennarddh.mindustry.genesis.core.GenesisAPI
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
import mindustry.gen.Player
import kotlin.time.Duration

class CommandsHandler : Handler {
    override suspend fun onInit() {
        GenesisAPI.commandRegistry.registerParameterType(Boolean::class, BooleanParameter())
        GenesisAPI.commandRegistry.registerParameterType(Char::class, CharParameter())
        GenesisAPI.commandRegistry.registerParameterType(String::class, StringParameter())

        GenesisAPI.commandRegistry.registerParameterType(Float::class, FloatParameter())
        GenesisAPI.commandRegistry.registerParameterType(Double::class, DoubleParameter())

        GenesisAPI.commandRegistry.registerParameterType(Byte::class, ByteParameter())
        GenesisAPI.commandRegistry.registerParameterType(Short::class, ShortParameter())
        GenesisAPI.commandRegistry.registerParameterType(Int::class, IntParameter())
        GenesisAPI.commandRegistry.registerParameterType(Long::class, LongParameter())

        GenesisAPI.commandRegistry.registerParameterType(UByte::class, UByteParameter())
        GenesisAPI.commandRegistry.registerParameterType(UShort::class, UShortParameter())
        GenesisAPI.commandRegistry.registerParameterType(UInt::class, UIntParameter())
        GenesisAPI.commandRegistry.registerParameterType(ULong::class, ULongParameter())

        GenesisAPI.commandRegistry.registerParameterType(Enum::class, EnumParameter())

        GenesisAPI.commandRegistry.registerParameterType(Duration::class, DurationParameter())
        GenesisAPI.commandRegistry.registerParameterType(Player::class, PlayerParameter())

        GenesisAPI.commandRegistry.registerParameterValidationAnnotation(
            GT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGT
        )
        GenesisAPI.commandRegistry.registerParameterValidationAnnotation(
            GTE::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGTE
        )
        GenesisAPI.commandRegistry.registerParameterValidationAnnotation(
            LT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateLT
        )
        GenesisAPI.commandRegistry.registerParameterValidationAnnotation(
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