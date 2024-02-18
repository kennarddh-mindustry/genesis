package com.github.kennarddh.mindustry.genesis.core.server.packets

import mindustry.net.NetConnection
import kotlin.reflect.KClass

data class ServerPacketListener<T : Any>(
    val packetType: KClass<T>,
    val handler: suspend (NetConnection, T) -> Boolean?,
    val runAnyway: Boolean
)
