package com.github.kennarddh.mindustry.genesis.common.handlers.commands

import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.BooleanParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.CharParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.EnumParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.StringParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.signed.floating.DoubleParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.signed.floating.FloatParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.signed.integer.ByteParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.signed.integer.IntParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.signed.integer.LongParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.signed.integer.ShortParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.unsigned.integer.UByteParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.unsigned.integer.UIntParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.unsigned.integer.ULongParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.types.numbers.unsigned.integer.UShortParameter
import com.github.kennarddh.mindustry.genesis.common.commands.parameters.validations.numbers.*
import com.github.kennarddh.mindustry.genesis.core.Genesis
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler

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