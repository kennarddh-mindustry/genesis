package com.github.kennarddh.mindustry.genesis.core.server.packets.annotations

import com.github.kennarddh.mindustry.genesis.core.commons.priority.PriorityEnum

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ServerPacketHandler(val priority: PriorityEnum)
