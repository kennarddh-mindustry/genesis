package com.github.kennarddh.mindustry.genesis.core.commands

import com.github.kennarddh.mindustry.genesis.core.commands.parameters.CommandParameterValidator

data class CommandParameterValidationResult<T>(
    val validator: CommandParameterValidator<T>,
    val isValid: Boolean,
    val exception: Exception?
)
