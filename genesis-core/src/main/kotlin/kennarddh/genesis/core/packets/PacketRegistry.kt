package kennarddh.genesis.core.packets

import kennarddh.genesis.core.handlers.Handler
import kennarddh.genesis.core.packets.annotations.PacketHandler
import kennarddh.genesis.core.packets.exceptions.InvalidPacketHandlerMethodException
import mindustry.Vars.netServer
import mindustry.gen.Player
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

class PacketRegistry {
    fun init() {
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val packetHandlerAnnotation = function.findAnnotation<PacketHandler>() ?: continue

            val names = packetHandlerAnnotation.names

            val functionParameters = function.parameters.drop(1)

            if (functionParameters.size == 3 || functionParameters.size == 2)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept 3 or 2 parameters")

            if (functionParameters[0].type.classifier != Player::class)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept player as the first parameter")

            if (functionParameters[1].type.classifier != String::class)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept string as the second parameter")

            if (functionParameters.size == 3 && functionParameters[2].type.classifier != String::class)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept string as the third parameter")

            names.forEach {
                netServer.addPacketHandler(it) { player, data ->
                    if (functionParameters.size == 3)
                        function.call(handler, player, data, it)
                    else
                        function.call(handler, player, data)
                }
            }
        }
    }
}