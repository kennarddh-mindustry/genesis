package com.github.kennarddh.mindustry.genesis.core.packets

import mindustry.gen.Player

data class PacketListener(val handler: suspend (Player, String) -> Boolean?, val runAnyway: Boolean)
