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
import mindustry.gen.Player
import kotlin.time.Duration

class CommandsHandler : Handler() {
    override fun onInit() {
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

        Genesis.commandRegistry.registerValidationAnnotation(
            GT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGT
        )
        Genesis.commandRegistry.registerValidationAnnotation(
            GTE::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateGTE
        )
        Genesis.commandRegistry.registerValidationAnnotation(
            LT::class, listOf(
                Float::class,
                Double::class,
                Byte::class,
                Short::class,
                Int::class,
                Long::class,
            ), ::validateLT
        )
        Genesis.commandRegistry.registerValidationAnnotation(
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