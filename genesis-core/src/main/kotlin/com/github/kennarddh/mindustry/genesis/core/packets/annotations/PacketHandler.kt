package com.github.kennarddh.mindustry.genesis.core.packets.annotations

import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PacketHandler(val names: Array<String>, val priority: Priority, val runAnyway: Boolean = false)
