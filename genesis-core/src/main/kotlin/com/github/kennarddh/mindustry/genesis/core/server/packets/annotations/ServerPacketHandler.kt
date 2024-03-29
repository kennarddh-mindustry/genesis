package com.github.kennarddh.mindustry.genesis.core.server.packets.annotations

import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ServerPacketHandler(
    val priority: Priority,
    val runAnyway: Boolean = false
)
