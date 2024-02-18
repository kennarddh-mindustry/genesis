package com.github.kennarddh.mindustry.genesis.core.packets

import mindustry.gen.Player

data class PacketListener(
    val packetType: String,
    val handler: suspend (Player, String) -> Boolean?,
    val runAnyway: Boolean
)
