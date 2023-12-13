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

            if (!(functionParameters.size == 1 || functionParameters.size == 2 || functionParameters.size == 3))
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept 1, 2 or 3 parameters")

            val acceptContent = functionParameters.size >= 2
            val acceptName = functionParameters.size == 3

            if (functionParameters[0].type.classifier != Player::class)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept player as the first parameter")

            if (acceptContent && functionParameters[1].type.classifier != String::class)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept string as the second parameter")

            if (acceptName && functionParameters[2].type.classifier != String::class)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept string as the third parameter")

            names.forEach {
                netServer.addPacketHandler(it) { player, data ->
                    if (acceptName)
                        function.call(handler, player, data, it)
                    else if (acceptContent)
                        function.call(handler, player, data)
                    else
                        function.call(handler, player)
                }
            }
        }
    }
}