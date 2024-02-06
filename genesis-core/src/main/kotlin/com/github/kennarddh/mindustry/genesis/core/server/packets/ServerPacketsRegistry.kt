package com.github.kennarddh.mindustry.genesis.core.server.packets

import arc.func.Cons2
import arc.struct.ObjectMap
import arc.util.Reflect
import com.github.kennarddh.mindustry.genesis.core.commons.CoroutineScopes
import com.github.kennarddh.mindustry.genesis.core.commons.priority.MutablePriorityList
import com.github.kennarddh.mindustry.genesis.core.commons.priority.PriorityContainer
import com.github.kennarddh.mindustry.genesis.core.commons.priority.PriorityEnum
import com.github.kennarddh.mindustry.genesis.core.events.exceptions.InvalidServerPacketHandlerMethodException
import com.github.kennarddh.mindustry.genesis.core.handlers.Handler
import com.github.kennarddh.mindustry.genesis.core.server.packets.annotations.ServerPacketHandler
import kotlinx.coroutines.launch
import mindustry.Vars.net
import mindustry.net.NetConnection
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible


class ServerPacketsRegistry {
    private val serverListeners: MutableMap<KClass<Any>, MutablePriorityList<suspend (NetConnection, Any) -> Boolean>> =
        mutableMapOf()

    internal fun init() {
    }

    private fun <T : Any> addServerListener(
        packetType: KClass<T>,
        priority: PriorityEnum,
        handler: suspend (NetConnection, T) -> Boolean
    ) {
        @Suppress("UNCHECKED_CAST")
        if (!serverListeners.contains(packetType as KClass<Any>)) {
            serverListeners[packetType] = MutablePriorityList()

            net.handleServer(packetType.java) { connection, packet ->
                val previousListeners =
                    Reflect.get<ObjectMap<Class<*>, Cons2<NetConnection, Any>>>(net, "serverListeners")

                val previousListener = previousListeners.get(packetType.java)

                CoroutineScopes.Main.launch {
                    var isStopped = false

                    serverListeners[packetType]!!.forEachPrioritized {
                        if (isStopped)
                            return@forEachPrioritized

                        val result = it(connection, packet)

                        if (!result)
                            isStopped = true
                    }

                    // Previous packet listener will always get called even if genesis listener failed
                    previousListener.get(connection, packet)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        serverListeners[packetType]!!.add(
            PriorityContainer(
                priority,
                handler as suspend (NetConnection, Any) -> Boolean
            )
        )
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val serverPacketHandlerAnnotation = function.findAnnotation<ServerPacketHandler>() ?: continue

            val priority = serverPacketHandlerAnnotation.priority

            val functionParameters = function.parameters.drop(1)

            if (functionParameters.size != 2)
                throw InvalidServerPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept exactly two parameters connection and the packet type")

            if (functionParameters[0].type.classifier != NetConnection::class)
                throw InvalidServerPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept connection as the first parameter")

            if (function.returnType.classifier != Boolean::class)
                throw InvalidServerPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return boolean")

            val packetType = functionParameters[1].type.classifier as KClass<*>

            addServerListener(packetType, priority) { connection, packet ->
                function.callSuspend(handler, connection, packet) as Boolean
            }
        }
    }
}