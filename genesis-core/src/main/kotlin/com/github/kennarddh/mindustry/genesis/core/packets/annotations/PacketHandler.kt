package com.github.kennarddh.mindustry.genesis.core.packets.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class PacketHandler(val names: Array<String>)
