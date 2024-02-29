package com.github.kennarddh.mindustry.genesis.core.server.packets

import arc.func.Cons2
import arc.struct.ObjectMap
import arc.util.Reflect
import com.github.kennarddh.mindustry.genesis.core.commons.CoroutineScopes
import com.github.kennarddh.mindustry.genesis.core.commons.priority.MutablePriorityList
import com.github.kennarddh.mindustry.genesis.core.commons.priority.Priority
import com.github.kennarddh.mindustry.genesis.core.commons.priority.PriorityContainer
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
    private val serverListeners: MutableMap<KClass<Any>, MutablePriorityList<ServerPacketListener<Any>>> =
        mutableMapOf()

    internal fun init() {
    }

    private fun <T : Any> addServerListener(
        packetType: KClass<T>,
        priority: Priority,
        runAnyway: Boolean,
        handler: suspend (NetConnection, T) -> Boolean?
    ) {
        @Suppress("UNCHECKED_CAST")
        if (!serverListeners.contains(packetType as KClass<Any>)) {
            serverListeners[packetType] = MutablePriorityList()

            val previousListeners =
                Reflect.get<ObjectMap<Class<*>, Cons2<NetConnection, Any>>>(net, "serverListeners")

            val previousListener = previousListeners.get(packetType.java)

            net.handleServer(packetType.java) { connection, packet ->
                CoroutineScopes.Main.launch {
                    var output = true

                    serverListeners[packetType]!!.forEachPrioritized {
                        if (it.runAnyway || output) {
                            val result = it.handler(connection, packet)

                            if (!it.runAnyway) {
                                output = result!!
                            }
                        }
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
                ServerPacketListener(
                    packetType as KClass<T>,
                    handler,
                    runAnyway
                )
            ) as PriorityContainer<ServerPacketListener<Any>>
        )
    }

    fun registerHandler(handler: Handler) {
        for (function in handler::class.declaredFunctions) {
            function.isAccessible = true

            val serverPacketHandlerAnnotation = function.findAnnotation<ServerPacketHandler>() ?: continue

            val priority = serverPacketHandlerAnnotation.priority
            val runAnyway = serverPacketHandlerAnnotation.runAnyway

            val functionParameters = function.parameters.drop(1)

            if (functionParameters.size != 2)
                throw InvalidServerPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept exactly two parameters connection and the packet type")

            if (functionParameters[0].type.classifier != NetConnection::class)
                throw InvalidServerPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must accept connection as the first parameter")

            if (function.returnType.classifier != Boolean::class)
                throw InvalidServerPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return boolean")

            if (!runAnyway && function.returnType.classifier != Boolean::class)
                throw InvalidServerPacketHandlerMethodException("Method ${handler::class.qualifiedName}.${function.name} must return boolean")

            val packetType = functionParameters[1].type.classifier as KClass<*>

            addServerListener(packetType, priority, runAnyway) { connection, packet ->
                val result = function.callSuspend(handler, connection, packet)

                if (runAnyway)
                    null
                else
                    result as Boolean
            }
        }
    }
}