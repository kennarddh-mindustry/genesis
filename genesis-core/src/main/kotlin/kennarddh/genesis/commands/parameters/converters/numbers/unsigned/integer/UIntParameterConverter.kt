package kennarddh.genesis.commands.parameters.converters.numbers.unsigned.integer

import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverter
import kennarddh.genesis.commands.parameters.converters.base.CommandParameterConverterParsingException

class UIntParameterConverter : CommandParameterConverter<UInt> {
    override fun parse(input: String): UInt {
        try {
            return input.toUInt()
        } catch (error: NumberFormatException) {
            throw CommandParameterConverterParsingException("Cannot convert $input into unsigned int for parameter :parameterName:.")
        }
    }
}