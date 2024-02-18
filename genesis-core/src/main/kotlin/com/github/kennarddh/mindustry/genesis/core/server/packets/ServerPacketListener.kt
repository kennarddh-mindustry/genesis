package com.github.kennarddh.mindustry.genesis.core.server.packets

import mindustry.net.NetConnection

data class ServerPacketListener(
    val handler: suspend (NetConnection, Any) -> Boolean,
    val runAnyway: Boolean
)
