package com.github.kennarddh.mindustry.genesis.core.packets

import com.github.kennarddh.mindustry.genesis.core.commons.CoroutineScopes
import com.github.kennarddh.mindustry.genesis.core.commons.priority.MutablePriorityList
import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority
import com.github.kennarddh.mindustry.genesis.core.commons.priority.PriorityContainer
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.packets.annotations.PacketHandler
import com.github.kennarddh.mindustry.genesis.core.packets.exceptions.InvalidPacketHandlerMethodException
import kotlinx.coroutines.launch
import mindustry.Vars.netServer
import mindustry.gen.Player
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

class PacketRegistry {
    private val packetListeners: MutableMap<String, MutablePriorityList<PacketListener>> =
        mutableMapOf()

    internal fun init() {
    }

    private fun addPacketListener(
        packetType: String,
        priority: Priority,
        runAnyway: Boolean,
        handler: suspend (Player, String) -> Boolean?,
    ) {
        if (!packetListeners.contains(packetType)) {
            packetListeners[packetType] = MutablePriorityList()

            netServer.addPacketHandler(packetType) { player, data ->
                CoroutineScopes.Main.launch {
                    var output = true

                    packetListeners[packetType]!!.forEachPrioritized {
                        if (it.runAnyway || output) {
                            val result = it.handler(player, data)

                            if (!it.runAnyway) {
                                output = result!!
                            }
                        }
                    }
                }
            }
        }

        packetListeners[packetType]!!.add(
            PriorityContainer(
                priority,
                PacketListener(packetType, handler, runAnyway)
            )
        )
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val packetHandlerAnnotation = function.findAnnotation<PacketHandler>() ?: continue

            val names = packetHandlerAnnotation.names
            val priority = packetHandlerAnnotation.priority
            val runAnyway = packetHandlerAnnotation.runAnyway

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

            if (!runAnyway && function.returnType.classifier != Boolean::class)
                throw InvalidPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return boolean")

            names.forEach {
                addPacketListener(it, priority, runAnyway) { player, data ->
                    val result = if (acceptName)
                        function.callSuspend(handler, player, data, it)
                    else if (acceptContent)
                        function.callSuspend(handler, player, data)
                    else
                        function.callSuspend(handler, player)

                    if (runAnyway)
                        null
                    else
                        result as Boolean
                }
            }
        }
    }
}