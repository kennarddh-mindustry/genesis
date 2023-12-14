package kennarddh.genesis.common.commands.parameters.types.numbers.signed.floating

import kennarddh.genesis.core.commands.parameters.types.CommandParameter
import kennarddh.genesis.core.commands.parameters.types.CommandParameterParsingException
import kotlin.reflect.KClass

class FloatParameter : CommandParameter<Float> {
    override fun parse(input: String): Float {
        try {
            return input.toFloat()
        } catch (error: NumberFormatException) {
            throw CommandParameterParsingException("Cannot convert $input into float for parameter :parameterName:.")
        }
    }

    override fun toUsageType(input: KClass<Float>): String = "float"
}