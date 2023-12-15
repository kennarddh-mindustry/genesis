package kennarddh.genesis.common.handlers.commands

import kennarddh.genesis.common.commands.parameters.types.BooleanParameter
import kennarddh.genesis.common.commands.parameters.types.CharParameter
import kennarddh.genesis.common.commands.parameters.types.EnumParameter
import kennarddh.genesis.common.commands.parameters.types.StringParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.signed.floating.DoubleParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.signed.floating.FloatParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer.ByteParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer.IntParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer.LongParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.signed.integer.ShortParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.unsigned.integer.UByteParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.unsigned.integer.UIntParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.unsigned.integer.ULongParameter
import kennarddh.genesis.common.commands.parameters.types.numbers.unsigned.integer.UShortParameter
import kennarddh.genesis.common.commands.parameters.validations.numbers.*
import kennarddh.genesis.core.Genesis
import kennarddh.genesis.core.handlers.Handler

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