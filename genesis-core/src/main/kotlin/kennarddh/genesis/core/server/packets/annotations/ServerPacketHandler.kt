package kennarddh.genesis.core.server.packets.annotations

import kennarddh.genesis.core.commons.priority.PriorityEnum

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ServerPacketHandler(val priority: PriorityEnum)
